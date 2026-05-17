package com.arkaly.arkalybackend.service;

import com.arkaly.arkalybackend.dto.*;
import com.arkaly.arkalybackend.models.*;
import com.arkaly.arkalybackend.models.enums.ReglaFinanciera;
import com.arkaly.arkalybackend.models.enums.TipoOperacion;
import com.arkaly.arkalybackend.models.enums.TipoTransaccion;
import com.arkaly.arkalybackend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InformeService {

    private final TransaccioneRepository transaccioneRepository;
    private final CategoriasTransaccioneRepository categoriaRepository;
    private final PerfilesFinancieroRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarteraRepository carteraRepository;
    private final InversionService inversionService;

    public InformeService(TransaccioneRepository transaccioneRepository,
                          CategoriasTransaccioneRepository categoriaRepository,
                          PerfilesFinancieroRepository perfilRepository,
                          UsuarioRepository usuarioRepository,
                          CarteraRepository carteraRepository,
                          InversionService inversionService) {
        this.transaccioneRepository = transaccioneRepository;
        this.categoriaRepository = categoriaRepository;
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.carteraRepository = carteraRepository;
        this.inversionService = inversionService;
    }

    // GASTOS POR CATEGORÍA

    // Desglose de gastos por categoría con importe y porcentaje sobre el total
    public List<GastoCategoriaDTO> getGastosPorCategoria(Integer idUsuario,
                                                         LocalDate desde, LocalDate hasta) {
        List<Transaccione> gastos = transaccioneRepository
                .findByIdUsuarioIdAndTipoAndFechaBetween(
                        idUsuario, TipoTransaccion.GASTO, desde, hasta);

        BigDecimal totalGeneral = gastos.stream()
                .map(Transaccione::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Integer, BigDecimal> porCategoria = new HashMap<>();
        Map<Integer, CategoriasTransaccione> catMap = new HashMap<>();

        for (Transaccione t : gastos) {
            if (t.getIdCategoria() == null) continue;
            Integer catId = t.getIdCategoria().getId();
            porCategoria.merge(catId, t.getCantidad(), BigDecimal::add);
            catMap.put(catId, t.getIdCategoria());
        }

        List<GastoCategoriaDTO> resultado = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : porCategoria.entrySet()) {
            CategoriasTransaccione cat = catMap.get(entry.getKey());
            double porcentaje = totalGeneral.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                    entry.getValue().multiply(BigDecimal.valueOf(100))
                            .divide(totalGeneral, 2, RoundingMode.HALF_UP).doubleValue();
            resultado.add(new GastoCategoriaDTO(
                    cat.getId(), cat.getNombre(), cat.getColor(),
                    cat.getIcono(), entry.getValue(), porcentaje));
        }

        resultado.sort((a, b) -> b.getTotal().compareTo(a.getTotal()));
        return resultado;
    }

    // INGRESOS VS GASTOS MENSUAL

    // Evolución mensual de ingresos y gastos, incluyendo ventas de inversiones
    public List<IngresoGastoMensualDTO> getIngresosVsGastos(Integer idUsuario,
                                                            LocalDate desde, LocalDate hasta) {
        List<Transaccione> todas = transaccioneRepository
                .findByIdUsuarioIdAndFechaBetweenOrderByFechaDesc(idUsuario, desde, hasta);

        Map<String, BigDecimal> ingresos = new TreeMap<>();
        Map<String, BigDecimal> gastos = new TreeMap<>();
        Map<String, int[]> claves = new TreeMap<>();

        for (Transaccione t : todas) {
            String key = t.getAnio() + "-" + String.format("%02d", t.getMes());
            claves.put(key, new int[]{t.getAnio(), t.getMes()});
            if (t.getTipo() == TipoTransaccion.INGRESO) {
                ingresos.merge(key, t.getCantidad(), BigDecimal::add);
            } else {
                gastos.merge(key, t.getCantidad(), BigDecimal::add);
            }
        }

        Map<String, BigDecimal> gananciasPorMes = new TreeMap<>();
        for (Cartera op : carteraRepository.findByIdUsuarioIdOrderByFechaOperacionDesc(idUsuario)) {
            LocalDate fecha = op.getFechaOperacion();
            if (fecha.isBefore(desde) || fecha.isAfter(hasta)) continue;
            if (op.getTipoOperacion() != TipoOperacion.VENTA) continue;

            String key = fecha.getYear() + "-" + String.format("%02d", fecha.getMonthValue());
            claves.put(key, new int[]{fecha.getYear(), fecha.getMonthValue()});
            gananciasPorMes.merge(key, op.getPrecioTotal(), BigDecimal::add);
        }

        List<IngresoGastoMensualDTO> resultado = new ArrayList<>();
        for (String key : claves.keySet()) {
            int[] ym = claves.get(key);
            BigDecimal ing = ingresos.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal gas = gastos.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal ganInv = gananciasPorMes.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal balance = ing.subtract(gas);

            String etiqueta = LocalDate.of(ym[0], ym[1], 1)
                    .getMonth().getDisplayName(TextStyle.SHORT, new Locale("es")) + " " + ym[0];

            IngresoGastoMensualDTO dto = new IngresoGastoMensualDTO(
                    ym[1], ym[0], etiqueta, ing, gas, balance);
            dto.setGananciaInversiones(ganInv);
            dto.setPerdidaInversiones(BigDecimal.ZERO);
            dto.setBalanceConInversiones(balance.add(ganInv));
            resultado.add(dto);
        }
        return resultado;
    }

    // REGLA FINANCIERA

    // Actualiza la regla financiera del usuario; crea el perfil si no existe
    public void actualizarReglaFinanciera(Integer idUsuario, String regla) {
        PerfilesFinanciero perfil = perfilRepository.findByIdUsuarioId(idUsuario)
                .orElseGet(() -> {
                    Optional<Usuario> resultado = usuarioRepository.findById(idUsuario);

                    if (resultado.isEmpty()) {
                        throw new RuntimeException("Usuario no encontrado");
                    }

                    PerfilesFinanciero nuevo = new PerfilesFinanciero();
                    nuevo.setIdUsuario(resultado.get());
                    return nuevo;
                });
        perfil.setReglaFinanciera(ReglaFinanciera.valueOf(regla));
        perfilRepository.save(perfil);
    }

    // PROYECCIÓN FINANCIERA

    // Proyección basada en los últimos 6 meses y la regla de ahorro del usuario
    public ProyeccionDTO getProyeccion(Integer idUsuario) {
        LocalDate hasta = LocalDate.now();
        LocalDate desde = hasta.minusMonths(6);

        List<Transaccione> todas = transaccioneRepository
                .findByIdUsuarioIdAndFechaBetweenOrderByFechaDesc(idUsuario, desde, hasta);

        BigDecimal totalIngresos = sumar(todas, TipoTransaccion.INGRESO);
        BigDecimal totalGastos = sumar(todas, TipoTransaccion.GASTO);
        BigDecimal mediaIng = totalIngresos.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
        BigDecimal mediaGas = totalGastos.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);

        String reglaEnum = perfilRepository.findByIdUsuarioId(idUsuario)
                .map(p -> p.getReglaFinanciera().name())
                .orElse("CINCUENTA_TREINTA_VEINTE");

        double[] splits = getSplits(reglaEnum);
        BigDecimal necesarios = mediaIng.multiply(BigDecimal.valueOf(splits[0])).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ocio       = mediaIng.multiply(BigDecimal.valueOf(splits[1])).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ahorroSug  = mediaIng.multiply(BigDecimal.valueOf(splits[2])).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ahorroReal = mediaIng.subtract(mediaGas).max(BigDecimal.ZERO);

        String reglaLabel = switch (reglaEnum) {
            case "PORCENTAJE_AHORRO" -> "70/20/10";
            case "KAKEIBO"           -> "60/20/20";
            default                  -> "50/30/20";
        };

        ResumenInversionDTO resumen = inversionService.getResumen(idUsuario);
        BigDecimal valorCartera   = orZero(resumen.getValorActual());
        BigDecimal totalInvertido = orZero(resumen.getTotalInvertido());
        BigDecimal pnlInversiones = valorCartera.subtract(totalInvertido);
        BigDecimal pnlPorcentaje  = orZero(resumen.getPorcentajeVariacion());

        ProyeccionDTO dto = new ProyeccionDTO(
                reglaLabel, mediaIng, mediaGas, ahorroSug, ahorroReal,
                necesarios, ocio,
                generarMensaje(ahorroReal, ahorroSug, mediaGas, mediaIng, pnlInversiones));
        dto.setValorCartera(valorCartera);
        dto.setTotalInvertido(totalInvertido);
        dto.setPnlInversiones(pnlInversiones);
        dto.setPnlPorcentaje(pnlPorcentaje);
        dto.setPatrimonioTotal(ahorroReal.add(valorCartera));
        dto.setNumeroPosiciones(resumen.getNumeroPosiciones());
        return dto;
    }

    // RESUMEN PATRIMONIAL

    // Patrimonio neto: liquidez de los últimos 6 meses + valor de la cartera
    public ResumenPatrimonialDTO getResumenPatrimonial(Integer idUsuario) {
        LocalDate hasta = LocalDate.now();
        LocalDate desde = hasta.minusMonths(6);

        List<Transaccione> todas = transaccioneRepository
                .findByIdUsuarioIdAndFechaBetweenOrderByFechaDesc(idUsuario, desde, hasta);

        BigDecimal liquidez = sumar(todas, TipoTransaccion.INGRESO)
                .subtract(sumar(todas, TipoTransaccion.GASTO));

        ResumenInversionDTO resumen = inversionService.getResumen(idUsuario);
        BigDecimal valorCartera   = orZero(resumen.getValorActual());
        BigDecimal totalInvertido = orZero(resumen.getTotalInvertido());
        BigDecimal pnl            = valorCartera.subtract(totalInvertido);
        BigDecimal pnlPct         = orZero(resumen.getPorcentajeVariacion());

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        BigDecimal gananciaRealizadaMes = carteraRepository
                .findByIdUsuarioIdOrderByFechaOperacionDesc(idUsuario).stream()
                .filter(op -> op.getTipoOperacion() == TipoOperacion.VENTA)
                .filter(op -> !op.getFechaOperacion().isBefore(inicioMes))
                .map(Cartera::getPrecioTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ResumenPatrimonialDTO dto = new ResumenPatrimonialDTO();
        dto.setLiquidez(liquidez);
        dto.setValorCartera(valorCartera);
        dto.setTotalInvertido(totalInvertido);
        dto.setPnlInversiones(pnl);
        dto.setPnlPorcentaje(pnlPct);
        dto.setPatrimonioNeto(liquidez.add(valorCartera));
        dto.setGananciaRealizadaMes(gananciaRealizadaMes);
        dto.setNumeroPosiciones(resumen.getNumeroPosiciones());
        dto.setValorPorTipo(resumen.getValorActualPorTipo() != null
                ? resumen.getValorActualPorTipo() : Map.of());
        return dto;
    }

    // HELPERS PRIVADOS

    // Suma las cantidades de las transacciones de un tipo concreto
    private BigDecimal sumar(List<Transaccione> transacciones, TipoTransaccion tipo) {
        return transacciones.stream()
                .filter(t -> t.getTipo() == tipo)
                .map(Transaccione::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Devuelve el valor o cero si es null
    private BigDecimal orZero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    // Porcentajes de la regla financiera: [necesarios, ocio, ahorro]
    private double[] getSplits(String regla) {
        return switch (regla) {
            case "PORCENTAJE_AHORRO" -> new double[]{0.70, 0.20, 0.10};
            case "KAKEIBO"           -> new double[]{0.60, 0.20, 0.20};
            default                  -> new double[]{0.50, 0.30, 0.20};
        };
    }

    // Genera un mensaje con la situación financiera del usuario
    private String generarMensaje(BigDecimal ahorroReal, BigDecimal ahorroSug,
                                  BigDecimal gastos, BigDecimal ingresos,
                                  BigDecimal pnlInversiones) {
        if (ingresos.compareTo(BigDecimal.ZERO) == 0) return "Sin datos suficientes.";

        StringBuilder sb = new StringBuilder();
        if (gastos.compareTo(ingresos) > 0) {
            sb.append("Tus gastos superan tus ingresos. Revisa tus categorías de gasto. ");
        } else if (ahorroReal.compareTo(ahorroSug) >= 0) {
            sb.append("¡Vas por buen camino! Estás cumpliendo tu objetivo de ahorro. ");
        } else {
            sb.append("Puedes mejorar tu ahorro reduciendo gastos en ocio o prescindibles. ");
        }

        if (pnlInversiones.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format(
                    "Tus inversiones están generando +%.2f € de rendimiento.", pnlInversiones));
        } else if (pnlInversiones.compareTo(BigDecimal.ZERO) < 0) {
            sb.append(String.format(
                    "Tus inversiones tienen una pérdida no realizada de %.2f €.", pnlInversiones));
        }

        return sb.toString().trim();
    }
}