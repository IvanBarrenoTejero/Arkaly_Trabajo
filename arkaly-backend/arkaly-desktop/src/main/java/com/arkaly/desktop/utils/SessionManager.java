package com.arkaly.desktop.utils;

/*
    Memoría temporal de la sesión del usuario mientras la app está abierta

    Todo en esta clase será static ya que se quiere que sea accesible desde cualquier punto sin tener que instanciarse
 */
public class SessionManager {

    private static String token;
    private static Integer idUsuario;
    private static String nombreUsuario;

    public static String getToken() { return token; }
    public static void setToken(String token) { SessionManager.token = token; }

    public static Integer getIdUsuario() { return idUsuario; }
    public static void setIdUsuario(Integer id) { SessionManager.idUsuario = id; }

    public static String getNombreUsuario() { return nombreUsuario; }
    public static void setNombreUsuario(String nombre) { SessionManager.nombreUsuario = nombre; }

    public static void cerrarSesion() {
        token = null;
        idUsuario = null;
        nombreUsuario = null;
    }
}