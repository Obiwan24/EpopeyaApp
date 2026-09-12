package com.example.epopeyaap.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HomeController
 * --------------
 * NUEVO. Tu index.html vive en /static/pages/index.html, no en la raíz de
 * /static/. Spring Boot solo sirve automáticamente en "/" el archivo que
 * esté exactamente en static/index.html (su "welcome page" por defecto);
 * como el tuyo está en una subcarpeta, quien entra a la URL pelada de tu
 * dominio (https://tuapp.onrender.com/) recibía un 404
 * (NoResourceFoundException) en vez de la portada.
 *
 * Este controlador simplemente redirige "/" hacia "/pages/index.html", así
 * que puedes compartir con tu equipo la URL raíz tal cual, sin tener que
 * decirles "añade /pages/index.html al final".
 *
 * OJO: es un @Controller normal (no @RestController): el string que
 * devuelve no es la respuesta en sí, es una instrucción de redirección que
 * interpreta Spring MVC.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/pages/index.html";
    }
}