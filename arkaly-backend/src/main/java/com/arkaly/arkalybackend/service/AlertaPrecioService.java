package com.arkaly.arkalybackend.service;

import com.arkaly.arkalybackend.clients.YFinanceClient;
import com.arkaly.arkalybackend.dto.AlertaPrecioDTO;
import com.arkaly.arkalybackend.models.AlertasPrecio;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.models.enums.Condicion;
import com.arkaly.arkalybackend.models.enums.TipoActivo;
import com.arkaly.arkalybackend.repositories.AlertaPrecioRepository;
import com.arkaly.arkalybackend.repositories.ActivoMercadoRepository;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de alertas de precio de activos financieros.
 * Comprueba en tiempo real si una alerta ha sido disparada consultando
 * el precio actual via Yahoo Finance.
 */
@Service
public class AlertaPrecioService {

    private final AlertaPrecioRepository alertaPrecioRepository;
    private final ActivoMercadoRepository activoMercadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final YFinanceClient yFinanceClient;

    public AlertaPrecioService(AlertaPrecioRepository alertaPrecioRepository,
                               ActivoMercadoRepository activoMercadoRepository,
                               UsuarioRepository usuarioRepository,
                               YFinanceClient yFinanceClient) {
        this.alertaPrecioRepository = alertaPrecioRepository;
        this.activoMercadoRepository = activoMercadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.yFinanceClient = yFinanceClient;
    }

    // CONSULTAS

    // Todas las alertas del usuario, de más reciente a más antigua
    public List<AlertaPrecioDTO> getAlertasPrecio(Integer idUsuario) {
        return alertaPrecioRepository.findByIdUsuarioIdOrderByFechaCreacionDesc(idUsuario)
                .stream().map(this::toAlertaPrecioDTO).toList();
    }

    // Alertas activas enriquecidas con precio actual y si han sido disparadas
    public List<AlertaPrecioDTO> getAlertasConPrecioActual(Integer idUsuario) {
        List<AlertasPrecio> alertasActivas = alertaPrecioRepository
                .findByIdUsuarioIdOrderByFechaCreacionDesc(idUsuario)
                .stream()
                .filter(AlertasPrecio::getActiva)
                .toList();

        BigDecimal usdToEur = yFinanceClient.getTipoCambioUSDtoEUR().orElse(BigDecimal.ONE);
        List<AlertaPrecioDTO> resultado = new ArrayList<>();

        for (AlertasPrecio alerta : alertasActivas) {
            AlertaPrecioDTO dto = toAlertaPrecioDTO(alerta);

            activoMercadoRepository.findBySimboloIgnoreCase(alerta.getSimbolo())
                    .ifPresent(activo -> dto.setNombre(activo.getNombre()));

            Optional<BigDecimal> precioOpt = getPrecioEnEur(alerta, usdToEur);
            precioOpt.ifPresent(precio -> {
                dto.setPrecioActual(precio);
                dto.setDisparada(estaDisparada(alerta, precio));
            });

            resultado.add(dto);
        }

        return resultado;
    }

    // Solo las alertas activas que han cruzado el precio objetivo
    public List<AlertaPrecioDTO> getAlertasDisparadas(Integer idUsuario) {
        List<AlertasPrecio> alertasActivas = alertaPrecioRepository
                .findByIdUsuarioIdOrderByFechaCreacionDesc(idUsuario)
                .stream()
                .filter(AlertasPrecio::getActiva)
                .toList();

        BigDecimal usdToEur = yFinanceClient.getTipoCambioUSDtoEUR().orElse(BigDecimal.ONE);
        List<AlertaPrecioDTO> disparadas = new ArrayList<>();

        for (AlertasPrecio alerta : alertasActivas) {
            getPrecioEnEur(alerta, usdToEur).ifPresent(precio -> {
                if (estaDisparada(alerta, precio)) {
                    AlertaPrecioDTO dto = toAlertaPrecioDTO(alerta);
                    dto.setPrecioActual(precio);
                    disparadas.add(dto);
                }
            });
        }

        return disparadas;
    }

    // OPERACIONES

    // Crea una nueva alerta de precio para el usuario
    public AlertasPrecio crearAlertaPrecio(Integer idUsuario, String simbolo,
                                           TipoActivo tipoActivo, String condicion,
                                           BigDecimal precioObjetivo) {
        Optional<Usuario> resultado = usuarioRepository.findById(idUsuario);

        if (resultado.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = resultado.get();

        AlertasPrecio alerta = new AlertasPrecio();
        alerta.setIdUsuario(usuario);
        alerta.setSimbolo(simbolo);
        alerta.setTipoActivo(tipoActivo);
        alerta.setCondicion(Condicion.valueOf(condicion));
        alerta.setPrecioObjetivo(precioObjetivo);
        alerta.setActiva(true);
        alerta.setFechaCreacion(Instant.now());
        return alertaPrecioRepository.save(alerta);
    }

    // Activa o desactiva una alerta alternando su estado
    public void toggleAlertaPrecio(Integer id) {
        Optional<AlertasPrecio> resultado = alertaPrecioRepository.findById(id);

        if (resultado.isEmpty()) {
            throw new RuntimeException("Alerta no encontrada");
        }

        AlertasPrecio alerta = resultado.get();
        alerta.setActiva(!alerta.getActiva());
        alertaPrecioRepository.save(alerta);
    }

    // Elimina una alerta por su id
    public void eliminarAlertaPrecio(Integer id) {
        alertaPrecioRepository.deleteById(id);
    }

    // HELPERS PRIVADOS

    // Precio actual del activo en EUR, aplicando conversión si cotiza en USD
    private Optional<BigDecimal> getPrecioEnEur(AlertasPrecio alerta, BigDecimal usdToEur) {
        String tipo = alerta.getTipoActivo().name();
        String simboloYF = yFinanceClient.formatearSimbolo(alerta.getSimbolo(), tipo);
        return yFinanceClient.getPrecioActual(simboloYF).map(precio ->
                yFinanceClient.esDivisaUSD(tipo)
                        ? precio.multiply(usdToEur).setScale(2, RoundingMode.HALF_UP)
                        : precio
        );
    }

    // Comprueba si el precio actual ha cruzado el precio objetivo
    private boolean estaDisparada(AlertasPrecio alerta, BigDecimal precioActual) {
        return switch (alerta.getCondicion()) {
            case MAYOR_QUE -> precioActual.compareTo(alerta.getPrecioObjetivo()) >= 0;
            case MENOR_QUE -> precioActual.compareTo(alerta.getPrecioObjetivo()) <= 0;
        };
    }

    // MAPPER

    private AlertaPrecioDTO toAlertaPrecioDTO(AlertasPrecio a) {
        AlertaPrecioDTO dto = new AlertaPrecioDTO();
        dto.setId(a.getId());
        dto.setSimbolo(a.getSimbolo());
        dto.setTipoActivo(a.getTipoActivo().name());
        dto.setCondicion(a.getCondicion().name());
        dto.setPrecioObjetivo(a.getPrecioObjetivo());
        dto.setActiva(a.getActiva());
        dto.setFechaCreacion(a.getFechaCreacion() != null
                ? LocalDateTime.ofInstant(a.getFechaCreacion(), ZoneId.systemDefault()) : null);
        dto.setFechaDisparada(a.getFechaDisparada() != null
                ? LocalDateTime.ofInstant(a.getFechaDisparada(), ZoneId.systemDefault()) : null);
        return dto;
    }
}