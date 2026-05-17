package com.arkaly.desktop.controllers;

import static com.arkaly.desktop.utils.Estilos.*;

import com.arkaly.desktop.services.InversionService;
import com.arkaly.desktop.utils.AsyncTask;
import com.arkaly.desktop.utils.InversionesUIHelper;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InversionesAlertasController {

    @FXML private VBox contenidoAlertas;
    @FXML private VBox seccionAlertas;
    @FXML private VBox seccionMetas;
    @FXML private VBox seccionCalculadora;

    @FXML public void initialize() {
        cargarAlertas();
        cargarMetas();
        construirCalculadora();
    }

    // ALERTAS

    private void cargarAlertas() {
        seccionAlertas.getChildren().clear();

        Label titulo = new Label("🔔  Alertas de precio");
        titulo.setStyle(TITULO);
        Button btnNueva = new Button("+ Nueva alerta");
        btnNueva.setStyle(BTN_PRIMARIO);
        btnNueva.setOnAction(e -> mostrarFormularioAlerta());

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox cabecera = new HBox(10, titulo, sp, btnNueva);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        Label cargando = new Label("⏳ Cargando alertas...");
        cargando.setStyle(FB_CARGANDO);
        seccionAlertas.getChildren().addAll(cabecera, new Separator(), cargando);

        final JsonNode[] alertas = {null};
        AsyncTask.ejecutar(
                () -> alertas[0] = InversionService.getAlertas(),
                () -> {
                    seccionAlertas.getChildren().remove(cargando);
                    if (alertas[0] == null || !alertas[0].isArray() || alertas[0].isEmpty()) {
                        seccionAlertas.getChildren().add(labelVacio("No tienes alertas configuradas"));
                        return;
                    }
                    for (JsonNode a : alertas[0]) seccionAlertas.getChildren().add(crearFilaAlerta(a));
                }
        );
    }

    private HBox crearFilaAlerta(JsonNode a) {
        int id = a.path("id").asInt();
        String simbolo = a.path("simbolo").asText();
        String condicion = a.path("condicion").asText();
        boolean activa = a.path("activa").asBoolean();
        boolean esMayor = "MAYOR_QUE".equals(condicion);

        Label lblSim = labelFila(InversionesUIHelper.emojiTipo(a.path("tipoActivo").asText()) + " " + simbolo, 120);
        Label lblCond = new Label(esMayor ? "▲ Mayor que" : "▼ Menor que");
        lblCond.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + (esMayor ? "#48bb78" : "#fc8181") + ";");
        lblCond.setPrefWidth(110);
        Label lblPrecio = labelFila(String.format("%.2f €", a.path("precioObjetivo").asDouble()), 100);
        Label lblEstado = new Label(activa ? "🟢 Activa" : "⚪ Pausada");
        lblEstado.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (activa ? "#48bb78" : "#a0aec0") + ";");
        lblEstado.setPrefWidth(80);

        Button btnToggle = new Button(activa ? "Pausar" : "Activar");
        btnToggle.setStyle(BTN_SECUNDARIO);
        btnToggle.setOnAction(e -> AsyncTask.ejecutar(() -> InversionService.toggleAlerta(id), this::cargarAlertas));

        Button btnElim = new Button("✕");
        btnElim.setStyle(BTN_ELIMINAR);
        btnElim.setOnAction(e -> AsyncTask.ejecutar(() -> InversionService.eliminarAlerta(id), this::cargarAlertas));

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox fila = new HBox(12, lblSim, lblCond, lblPrecio, lblEstado, sp, btnToggle, btnElim);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPadding(new Insets(10, 14, 10, 14));
        fila.setStyle(FILA_NORMAL);
        hoverFila(fila);
        return fila;
    }

    private void mostrarFormularioAlerta() {
        if (seccionAlertas.lookup("#formAlerta") != null) return;
        VBox form = new VBox(10);
        form.setId("formAlerta");
        form.setPadding(new Insets(14));
        form.setStyle(FILA_NORMAL.replace("#e2e8f0", "#bee3f8"));

        TextField campoSimbolo = campo("Símbolo (ej: BTC-USD, AAPL)");
        ComboBox<String> comboTipo = combo("CRIPTO", "ACCION", "FONDO");
        ComboBox<String> comboCond = combo("Mayor que", "Menor que");
        TextField campoPrecio = campo("Precio objetivo (€)");
        Label lblFb = new Label("");

        Button btnCrear = new Button("Crear alerta"); btnCrear.setStyle(BTN_PRIMARIO);
        Button btnCancel = new Button("Cancelar"); btnCancel.setStyle(BTN_SECUNDARIO);
        btnCancel.setOnAction(e -> seccionAlertas.getChildren().remove(form));

        btnCrear.setOnAction(e -> {
            String sim = campoSimbolo.getText().trim().toUpperCase();
            String prec = campoPrecio.getText().trim().replace(",", ".");
            if (sim.isEmpty() || prec.isEmpty()) { lblFb.setText("⚠️ Completa todos"); lblFb.setStyle(FB_WARN_SM); return; }
            try { Double.parseDouble(prec); } catch (NumberFormatException ex) { lblFb.setText("⚠️ Precio no válido"); lblFb.setStyle(FB_ERROR_SM); return; }
            String cond = comboCond.getValue().equals("Mayor que") ? "MAYOR_QUE" : "MENOR_QUE";
            btnCrear.setDisable(true); lblFb.setText("⏳ Creando..."); lblFb.setStyle(FB_CARGANDO_SM);
            AsyncTask.ejecutar(() -> InversionService.crearAlerta(sim, comboTipo.getValue(), cond, prec), this::cargarAlertas);
        });

        form.getChildren().addAll(new Label("Nueva alerta de precio") {{ setStyle(SUBTITULO); }},
                new HBox(10, campoSimbolo, comboTipo, comboCond, campoPrecio), new HBox(10, btnCrear, btnCancel), lblFb);
        seccionAlertas.getChildren().add(2, form);
    }

    // METAS

    private void cargarMetas() {
        seccionMetas.getChildren().clear();

        Label titulo = new Label("🎯  Metas de ahorro"); titulo.setStyle(TITULO);
        Button btnNueva = new Button("+ Nueva meta"); btnNueva.setStyle(BTN_VERDE);
        btnNueva.setOnAction(e -> mostrarFormularioMeta());

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox cabecera = new HBox(10, titulo, sp, btnNueva); cabecera.setAlignment(Pos.CENTER_LEFT);
        Label cargando = new Label("⏳ Cargando metas..."); cargando.setStyle(FB_CARGANDO);
        seccionMetas.getChildren().addAll(cabecera, new Separator(), cargando);

        final JsonNode[] metas = {null};
        AsyncTask.ejecutar(
                () -> metas[0] = InversionService.getMetas(),
                () -> {
                    seccionMetas.getChildren().remove(cargando);
                    if (metas[0] == null || !metas[0].isArray() || metas[0].isEmpty()) {
                        seccionMetas.getChildren().add(labelVacio("No tienes metas de ahorro")); return;
                    }
                    for (JsonNode m : metas[0]) seccionMetas.getChildren().add(crearFilaMeta(m));
                }
        );
    }

    private VBox crearFilaMeta(JsonNode m) {
        int id = m.path("id").asInt();
        double objetivo = m.path("cantidadObjetivo").asDouble();
        double actual = m.path("cantidadActual").asDouble();
        boolean completada = m.path("completada").asBoolean();
        String fechaLimite = m.path("fechaLimite").asText("");
        double progreso = Math.min(objetivo > 0 ? (actual / objetivo) * 100 : 0, 100);
        String colorProg = completada ? "#48bb78" : "#4a90e2";

        Label lblNom = new Label((completada ? "✅ " : "🎯 ") + m.path("nombre").asText());
        lblNom.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label lblCant = new Label(String.format("%.2f € / %.2f €", actual, objetivo));
        lblCant.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");
        Label lblPct = new Label(String.format("%.1f%%", progreso));
        lblPct.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + colorProg + ";");

        ProgressBar barra = new ProgressBar(progreso / 100);
        barra.setPrefWidth(Double.MAX_VALUE); barra.setPrefHeight(8);
        barra.setStyle("-fx-accent: " + colorProg + ";"); HBox.setHgrow(barra, Priority.ALWAYS);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        HBox filaTop = new HBox(10, lblNom, sp, lblCant, lblPct); filaTop.setAlignment(Pos.CENTER_LEFT);

        HBox botones = new HBox(8); botones.setAlignment(Pos.CENTER_LEFT);
        if (!completada) {
            TextField campoAp = new TextField(); campoAp.setPromptText("Aportar €");
            campoAp.setPrefWidth(100); campoAp.setStyle(CAMPO_BLANCO + "; -fx-font-size: 11px");
            Button btnAp = new Button("Aportar");
            btnAp.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 6px; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
            btnAp.setOnAction(e -> {
                try {
                    double nueva = actual + Double.parseDouble(campoAp.getText().trim().replace(",", "."));
                    AsyncTask.ejecutar(() -> InversionService.actualizarMeta(id, nueva), this::cargarMetas);
                } catch (NumberFormatException ex) {}
            });
            botones.getChildren().addAll(campoAp, btnAp);
        }
        if (!fechaLimite.isEmpty() && !fechaLimite.equals("null")) {
            Label lblF = new Label("📅 " + fechaLimite); lblF.setStyle("-fx-font-size: 10px; -fx-text-fill: #a0aec0;");
            botones.getChildren().add(lblF);
        }
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
        Button btnElim = new Button("✕"); btnElim.setStyle(BTN_ELIMINAR);
        btnElim.setOnAction(e -> AsyncTask.ejecutar(() -> InversionService.eliminarMeta(id), this::cargarMetas));
        botones.getChildren().addAll(sp2, btnElim);

        VBox tarjeta = new VBox(8, filaTop, barra, botones);
        tarjeta.setPadding(new Insets(12, 14, 12, 14)); tarjeta.setStyle(FILA_NORMAL);
        return tarjeta;
    }

    private void mostrarFormularioMeta() {
        if (seccionMetas.lookup("#formMeta") != null) return;
        VBox form = new VBox(10); form.setId("formMeta");
        form.setPadding(new Insets(14)); form.setStyle(FILA_NORMAL.replace("#e2e8f0", "#bee3f8"));

        TextField campoNom = campo("Nombre de la meta");
        TextField campoCant = campo("Cantidad objetivo (€)");
        TextField campoFecha = campo("Fecha límite (YYYY-MM-DD, opcional)");
        Label lblFb = new Label("");
        Button btnCrear = new Button("Crear meta"); btnCrear.setStyle(BTN_VERDE);
        Button btnCancel = new Button("Cancelar"); btnCancel.setStyle(BTN_SECUNDARIO);
        btnCancel.setOnAction(e -> seccionMetas.getChildren().remove(form));

        btnCrear.setOnAction(e -> {
            String nom = campoNom.getText().trim();
            String cant = campoCant.getText().trim().replace(",", ".");
            String fecha = campoFecha.getText().trim();
            if (nom.isEmpty() || cant.isEmpty()) { lblFb.setText("⚠️ Obligatorios"); lblFb.setStyle(FB_WARN_SM); return; }
            try { Double.parseDouble(cant); } catch (NumberFormatException ex) { lblFb.setText("⚠️ Cantidad no válida"); lblFb.setStyle(FB_ERROR_SM); return; }
            btnCrear.setDisable(true); lblFb.setText("⏳ Creando..."); lblFb.setStyle(FB_CARGANDO_SM);
            AsyncTask.ejecutar(() -> InversionService.crearMeta(nom, cant, fecha.isEmpty() ? "" : fecha), this::cargarMetas);
        });

        form.getChildren().addAll(new Label("Nueva meta de ahorro") {{ setStyle(SUBTITULO); }},
                new HBox(10, campoNom, campoCant, campoFecha), new HBox(10, btnCrear, btnCancel), lblFb);
        seccionMetas.getChildren().add(2, form);
    }

    // CALCULADORA

    private void construirCalculadora() {
        seccionCalculadora.getChildren().clear();
        Label titulo = new Label("🖩  Calculadora de interés compuesto"); titulo.setStyle(TITULO);
        seccionCalculadora.getChildren().addAll(titulo, new Separator());

        TextField cIni = campo("Capital inicial (€)"), cAp = campo("Aporte mensual (€)"),
                cTasa = campo("Tasa anual (%)"), cAnios = campo("Años");
        HBox camposRow = new HBox(10, cIni, cAp, cTasa, cAnios); camposRow.setAlignment(Pos.CENTER_LEFT);

        VBox resBox = new VBox(8); resBox.setPadding(new Insets(14)); resBox.setStyle(FILA_NORMAL);
        resBox.setVisible(false); resBox.setManaged(false);
        Label lblResTit = new Label("📊  Resultado"); lblResTit.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        Label lblCapFin = new Label(""); lblCapFin.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #48bb78;");
        Label lblDesg = new Label(""); lblDesg.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;"); lblDesg.setWrapText(true);
        resBox.getChildren().addAll(lblResTit, lblCapFin, lblDesg);

        Button btnCalc = new Button("Calcular"); btnCalc.setStyle(BTN_PRIMARIO);
        Label lblErr = new Label(""); lblErr.setStyle(FB_ERROR);

        btnCalc.setOnAction(e -> {
            lblErr.setText("");
            try {
                double cap = parseDec(cIni), ap = parseDec(cAp), tasa = parseDec(cTasa);
                int anios = Integer.parseInt(cAnios.getText().trim());
                if (anios <= 0 || anios > 100) { lblErr.setText("⚠️ Años entre 1 y 100"); return; }
                double tM = tasa / 100 / 12; int meses = anios * 12;
                double totAp = cap + (ap * meses);
                double capFin = tM == 0 ? totAp : cap * Math.pow(1 + tM, meses) + ap * (Math.pow(1 + tM, meses) - 1) / tM;
                double inter = capFin - totAp;
                lblCapFin.setText(BigDecimal.valueOf(capFin).setScale(2, RoundingMode.HALF_UP).toPlainString() + " €");
                lblDesg.setText(String.format("Capital: %.2f €  ·  Aportaciones: %.2f € (%d×%.2f €)  ·  Total: %s €  ·  Intereses: %s €  ·  Rent: %.1f%%",
                        cap, ap * meses, meses, ap, BigDecimal.valueOf(totAp).setScale(2, RoundingMode.HALF_UP).toPlainString(),
                        BigDecimal.valueOf(inter).setScale(2, RoundingMode.HALF_UP).toPlainString(), totAp > 0 ? (inter / totAp) * 100 : 0));
                resBox.setVisible(true); resBox.setManaged(true);
            } catch (NumberFormatException ex) { lblErr.setText("⚠️ Valores numéricos"); resBox.setVisible(false); resBox.setManaged(false); }
        });

        seccionCalculadora.getChildren().addAll(camposRow, new HBox(10, btnCalc, lblErr) {{ setAlignment(Pos.CENTER_LEFT); }}, resBox);
    }

    // UTILIDADES

    private TextField campo(String ph) { TextField t = new TextField(); t.setPromptText(ph); t.setStyle(CAMPO_BLANCO); return t; }
    private ComboBox<String> combo(String... ops) { ComboBox<String> c = new ComboBox<>(); c.getItems().addAll(ops); c.getSelectionModel().selectFirst(); return c; }
    private Label labelFila(String txt, double ancho) { Label l = new Label(txt); l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2d3748;"); l.setPrefWidth(ancho); return l; }
    private Label labelVacio(String txt) { Label l = new Label(txt); l.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 13px;"); return l; }
    private double parseDec(TextField c) { return Double.parseDouble(c.getText().trim().replace(",", ".")); }

    // NAVEGACIÓN

    @FXML public void mostrarGeneral()   { navegar("Inversiones_General.fxml"); }
    @FXML public void mostrarCartera()   { navegar("Inversiones_Cartera.fxml"); }
    @FXML public void mostrarOrdenes()   { navegar("Inversiones_Ordenes.fxml"); }
    @FXML public void mostrarHistorial() { navegar("Inversiones_Historial.fxml"); }

    private void navegar(String fxml) {
        try {
            javafx.scene.Node node = new javafx.fxml.FXMLLoader(getClass().getResource("/com/arkaly/desktop/" + fxml)).load();
            StackPane parent = (StackPane) contenidoAlertas.getScene().lookup("#contenidoPanel");
            if (parent != null) parent.getChildren().setAll(node);
        } catch (Exception e) { e.printStackTrace(); }
    }
}