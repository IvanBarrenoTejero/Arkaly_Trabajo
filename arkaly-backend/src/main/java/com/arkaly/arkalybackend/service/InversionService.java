package com.arkaly.arkalybackend.service;

import com.arkaly.arkalybackend.clients.YFinanceClient;
import com.arkaly.arkalybackend.dto.CarteraDTO;
import com.arkaly.arkalybackend.dto.PosicionDTO;
import com.arkaly.arkalybackend.dto.PuntoHistoricoDTO;
import com.arkaly.arkalybackend.dto.ResumenInversionDTO;
import com.arkaly.arkalybackend.models.Cartera;
import com.arkaly.arkalybackend.models.Posicione;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.models.enums.TipoActivo;
import com.arkaly.arkalybackend.models.enums.TipoOperacion;
import com.arkaly.arkalybackend.repositories.CarteraRepository;
import com.arkaly.arkalybackend.repositories.PosicionRepository;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio principal de inversiones.
 * Gestiona las posiciones abiertas del usuario, el historial de operaciones
 * (compras/ventas) y el resumen global de la cartera con precios en tiempo real.
 */
@Service
public class InversionService {

    private final PosicionRepository posicionRepository;
    private final CarteraRepository carteraRepository;
    private final UsuarioRepository usuarioRepository;
    private final YFinanceClient yFinanceClient;

    public InversionService(PosicionRepository posicionRepository,
                            CarteraRepository carteraRepository,
                            UsuarioRepository usuarioRepository,
                            YFinanceClient yFinanceClient) {
        this.posicionRepository = posicionRepository;
        this.carteraRepository = carteraRepository;
        this.usuarioRepository = usuarioRepository;
        this.yFinanceClient = yFinanceClient;
    }

    // RESUMEN

    // Resumen global de la cartera: invertido, valor actual y desglose por tipo
    public ResumenInversionDTO getResumen(Integer idUsuario) {
        List<Posicione> posiciones = posicionRepository.findByIdUsuarioId(idUsuario);
        BigDecimal usdToEur = yFinanceClient.getTipoCambioUSDtoEUR().orElse(BigDecimal.ONE);

        BigDecimal totalInvertido = BigDecimal.ZERO;
        BigDecimal valorActual = BigDecimal.ZERO;
        Map<String, BigDecimal> porTipo = new HashMap<>();
        Map<String, BigDecimal> valorPorTipo = new HashMap<>();
        Map<String, BigDecimal> porcentajePorTipo = new HashMap<>();

        for (Posicione p : posiciones) {
            String tipo = p.getTipoActivo().name();
            BigDecimal totalInvertidoPos = convertirAEurSiUSD(p.getTotalInvertido(), tipo, usdToEur);

            totalInvertido = totalInvertido.add(totalInvertidoPos);
            porTipo.merge(tipo, totalInvertidoPos, BigDecimal::add);

            String simboloYF = yFinanceClient.formatearSimbolo(p.getSimbolo(), tipo);
            BigDecimal valorPos = yFinanceClient.getPrecioActual(simboloYF)
                    .map(precio -> p.getCantidadTotal()
                            .multiply(convertirAEurSiUSD(precio, tipo, usdToEur))
                            .setScale(2, RoundingMode.HALF_UP))
                    .orElse(totalInvertidoPos);

            valorActual = valorActual.add(valorPos);
            valorPorTipo.merge(tipo, valorPos, BigDecimal::add);
        }

        for (String tipo : porTipo.keySet()) {
            BigDecimal invertido = porTipo.get(tipo);
            BigDecimal actual = valorPorTipo.getOrDefault(tipo, invertido);
            if (invertido.compareTo(BigDecimal.ZERO) > 0) {
                porcentajePorTipo.put(tipo, calcularPorcentaje(actual, invertido));
            }
        }

        ResumenInversionDTO dto = new ResumenInversionDTO();
        dto.setTotalInvertido(totalInvertido);
        dto.setValorActual(valorActual);
        dto.setPorcentajeVariacion(totalInvertido.compareTo(BigDecimal.ZERO) > 0
                ? calcularPorcentaje(valorActual, totalInvertido) : BigDecimal.ZERO);
        dto.setTotalPorTipo(porTipo);
        dto.setValorActualPorTipo(valorPorTipo);
        dto.setPorcentajePorTipo(porcentajePorTipo);
        dto.setNumeroPosiciones(posiciones.size());
        return dto;
    }

    // HISTÓRICO

    // Histórico de precios de un activo por periodo e intervalo
    public List<PuntoHistoricoDTO> getHistoricoActivo(String simbolo, String tipoActivo,
                                                      String period, String interval) {
        String simboloYF = yFinanceClient.formatearSimbolo(simbolo, tipoActivo);
        return yFinanceClient.getHistoricoConFechas(simboloYF, period, interval);
    }

    // POSICIONES

    // Posiciones del usuario sin precio actualizado
    public List<PosicionDTO> getPosiciones(Integer idUsuario) {
        return posicionRepository.findByIdUsuarioId(idUsuario).stream()
                .map(this::toPosicionDTO).toList();
    }

    // Posiciones filtradas por tipo de activo
    public List<PosicionDTO> getPosicionesPorTipo(Integer idUsuario, TipoActivo tipo) {
        return posicionRepository.findByIdUsuarioIdAndTipoActivo(idUsuario, tipo).stream()
                .map(this::toPosicionDTO).toList();
    }

    // Posiciones con valor actual, PnL y PnL% en tiempo real
    public List<PosicionDTO> getPosicionesConValorActual(Integer idUsuario) {
        BigDecimal usdToEur = yFinanceClient.getTipoCambioUSDtoEUR().orElse(BigDecimal.ONE);
        return posicionRepository.findByIdUsuarioId(idUsuario).stream()
                .map(p -> enriquecerConPrecioActual(p, usdToEur))
                .toList();
    }

    // Posiciones de un tipo concreto con valor actual en tiempo real
    public List<PosicionDTO> getPosicionesConValorActualPorTipo(Integer idUsuario, TipoActivo tipo) {
        BigDecimal usdToEur = yFinanceClient.getTipoCambioUSDtoEUR().orElse(BigDecimal.ONE);
        return posicionRepository.findByIdUsuarioIdAndTipoActivo(idUsuario, tipo).stream()
                .map(p -> enriquecerConPrecioActual(p, usdToEur))
                .toList();
    }

    // OPERACIONES

    // Historial de operaciones del usuario
    public List<CarteraDTO> getOperaciones(Integer idUsuario) {
        return carteraRepository.findByIdUsuarioIdOrderByFechaOperacionDesc(idUsuario)
                .stream().map(this::toOperacionDTO).toList();
    }

    // Historial de operaciones filtrado por tipo de activo
    public List<CarteraDTO> getOperacionesPorTipo(Integer idUsuario, TipoActivo tipo) {
        return carteraRepository
                .findByIdUsuarioIdAndTipoActivoOrderByFechaOperacionDesc(idUsuario, tipo)
                .stream().map(this::toOperacionDTO).toList();
    }

    // Registra una compra o venta y actualiza la posición del usuario
    public Cartera registrarOperacion(Integer idUsuario, String simbolo,
                                      TipoActivo tipoActivo, TipoOperacion tipoOperacion,
                                      BigDecimal cantidad, BigDecimal precioUnitario,
                                      String notas) {
        Optional<Usuario> resultadoUsuario = usuarioRepository.findById(idUsuario);

        if (resultadoUsuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = resultadoUsuario.get();

        if (tipoOperacion == TipoOperacion.VENTA) {
            Optional<Posicione> resultadoPos = posicionRepository
                    .findByIdUsuarioIdAndSimbolo(idUsuario, simbolo);

            if (resultadoPos.isEmpty()) {
                throw new RuntimeException("No tienes posición abierta en " + simbolo);
            }

            Posicione pos = resultadoPos.get();

            if (cantidad.compareTo(pos.getCantidadTotal()) > 0) {
                throw new RuntimeException(String.format(
                        "No puedes vender %.4f %s, solo tienes %.4f",
                        cantidad, simbolo, pos.getCantidadTotal()));
            }
        }

        BigDecimal precioTotal = cantidad.multiply(precioUnitario).setScale(2, RoundingMode.HALF_UP);

        Cartera cartera = new Cartera();
        cartera.setIdUsuario(usuario);
        cartera.setSimbolo(simbolo);
        cartera.setTipoActivo(tipoActivo);
        cartera.setTipoOperacion(tipoOperacion);
        cartera.setCantidad(cantidad);
        cartera.setPrecioUnitario(precioUnitario);
        cartera.setPrecioTotal(precioTotal);
        cartera.setFechaOperacion(java.time.LocalDate.now());
        cartera.setNotas(notas);
        carteraRepository.save(cartera);

        actualizarPosicion(idUsuario, simbolo, tipoActivo, tipoOperacion,
                cantidad, precioUnitario, precioTotal);

        return cartera;
    }

    // Actualiza o crea la posición tras una operación (compra suma, venta resta)
    private void actualizarPosicion(Integer idUsuario, String simbolo,
                                    TipoActivo tipoActivo, TipoOperacion tipoOperacion,
                                    BigDecimal cantidad, BigDecimal precioUnitario,
                                    BigDecimal precioTotal) {
        Optional<Posicione> posOpt = posicionRepository
                .findByIdUsuarioIdAndSimbolo(idUsuario, simbolo);

        if (tipoOperacion == TipoOperacion.COMPRA) {
            if (posOpt.isPresent()) {
                Posicione pos = posOpt.get();
                BigDecimal nuevaCantidad = pos.getCantidadTotal().add(cantidad);
                BigDecimal nuevoTotal = pos.getTotalInvertido().add(precioTotal);
                pos.setCantidadTotal(nuevaCantidad);
                pos.setTotalInvertido(nuevoTotal);
                pos.setPrecioMedioCompra(nuevoTotal.divide(nuevaCantidad, 2, RoundingMode.HALF_UP));
                pos.setFechaActualizacion(Instant.now());
                posicionRepository.save(pos);
            } else {
                Optional<Usuario> resultadoUsuario = usuarioRepository.findById(idUsuario);

                if (resultadoUsuario.isEmpty()) {
                    throw new RuntimeException("Usuario no encontrado");
                }

                Usuario usuario = resultadoUsuario.get();
                Posicione pos = new Posicione();
                pos.setIdUsuario(usuario);
                pos.setSimbolo(simbolo);
                pos.setTipoActivo(tipoActivo);
                pos.setCantidadTotal(cantidad);
                pos.setPrecioMedioCompra(precioUnitario);
                pos.setTotalInvertido(precioTotal);
                pos.setFechaActualizacion(Instant.now());
                posicionRepository.save(pos);
            }
        } else {
            posOpt.ifPresent(pos -> {
                BigDecimal nuevaCantidad = pos.getCantidadTotal().subtract(cantidad);
                if (nuevaCantidad.compareTo(BigDecimal.ZERO) <= 0) {
                    posicionRepository.delete(pos);
                } else {
                    pos.setCantidadTotal(nuevaCantidad);
                    pos.setTotalInvertido(nuevaCantidad
                            .multiply(pos.getPrecioMedioCompra())
                            .setScale(2, RoundingMode.HALF_UP));
                    pos.setFechaActualizacion(Instant.now());
                    posicionRepository.save(pos);
                }
            });
        }
    }

    // HELPERS PRIVADOS

    // Añade valor actual, PnL y PnL% a una posición consultando el precio en tiempo real
    private PosicionDTO enriquecerConPrecioActual(Posicione p, BigDecimal usdToEur) {
        PosicionDTO dto = toPosicionDTO(p);
        String tipo = p.getTipoActivo().name();
        String simboloYF = yFinanceClient.formatearSimbolo(p.getSimbolo(), tipo);

        yFinanceClient.getPrecioActual(simboloYF).ifPresent(precio -> {
            BigDecimal precioEur = convertirAEurSiUSD(precio, tipo, usdToEur);
            BigDecimal totalInvertidoEur = convertirAEurSiUSD(p.getTotalInvertido(), tipo, usdToEur);
            BigDecimal valorActual = p.getCantidadTotal()
                    .multiply(precioEur)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal pnl = valorActual.subtract(totalInvertidoEur);
            BigDecimal pnlPct = totalInvertidoEur.compareTo(BigDecimal.ZERO) > 0
                    ? calcularPorcentaje(valorActual, totalInvertidoEur)
                    : BigDecimal.ZERO;

            dto.setValorActual(valorActual);
            dto.setPnl(pnl);
            dto.setPnlPorcentaje(pnlPct);
        });

        return dto;
    }

    // Convierte a EUR si el activo cotiza en USD
    private BigDecimal convertirAEurSiUSD(BigDecimal importe, String tipo, BigDecimal usdToEur) {
        return yFinanceClient.esDivisaUSD(tipo)
                ? importe.multiply(usdToEur).setScale(2, RoundingMode.HALF_UP)
                : importe;
    }

    // Calcula el porcentaje de variación entre valor actual y base
    private BigDecimal calcularPorcentaje(BigDecimal actual, BigDecimal base) {
        return actual.subtract(base)
                .divide(base, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // MAPPERS

    private PosicionDTO toPosicionDTO(Posicione p) {
        PosicionDTO dto = new PosicionDTO();
        dto.setId(p.getId());
        dto.setSimbolo(p.getSimbolo());
        dto.setTipoActivo(p.getTipoActivo().name());
        dto.setCantidadTotal(p.getCantidadTotal());
        dto.setPrecioMedioCompra(p.getPrecioMedioCompra());
        dto.setTotalInvertido(p.getTotalInvertido());
        carteraRepository
                .findFirstByIdUsuarioIdAndSimboloOrderByFechaOperacionAsc(
                        p.getIdUsuario().getId(), p.getSimbolo())
                .ifPresent(op -> dto.setFechaApertura(op.getFechaOperacion()));
        return dto;
    }

    private CarteraDTO toOperacionDTO(Cartera o) {
        CarteraDTO dto = new CarteraDTO();
        dto.setId(o.getId());
        dto.setSimbolo(o.getSimbolo());
        dto.setTipoActivo(o.getTipoActivo().name());
        dto.setTipoOperacion(o.getTipoOperacion().name());
        dto.setCantidad(o.getCantidad());
        dto.setPrecioUnitario(o.getPrecioUnitario());
        dto.setPrecioTotal(o.getPrecioTotal());
        dto.setFechaOperacion(o.getFechaOperacion());
        dto.setNotas(o.getNotas());
        return dto;
    }
}