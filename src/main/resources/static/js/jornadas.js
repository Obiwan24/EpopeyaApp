/**
 * jornadas.js - Listado de jornadas con sus partidos desplegables.
 * El estado de cada jornada se calcula en el backend a partir del estado
 * de sus 3 partidos (ver reglas en JornadaService).
 */

document.addEventListener("DOMContentLoaded", () => {
    exigirSesion();
    const usuario = getUsuarioActual();
    const listaEl = document.getElementById("lista-jornadas");

    async function cargarJornadas() {
        listaEl.innerHTML = "Cargando...";
        try {
            const jornadas = await apiFetch("/jornadas");
            listaEl.innerHTML = "";

            jornadas.forEach(j => {
                const card = document.createElement("div");
                card.className = "tarjeta jornada-card";
                card.innerHTML = `
                    <div class="cabecera-jornada">
                        <strong>Jornada ${j.numero} - vs ${j.rival}</strong>
                        <span class="etiqueta ${j.estado.toLowerCase()}">${formatearEstado(j.estado)}</span>
                    </div>
                    <small>${j.fechaHoraElegida ? new Date(j.fechaHoraElegida).toLocaleString() : "Fecha por confirmar"}</small>
                    <div class="partidos-contenedor oculto"></div>
                `;

                card.addEventListener("click", async (e) => {
                    const cont = card.querySelector(".partidos-contenedor");
                    if (!cont.classList.contains("oculto")) {
                        cont.classList.add("oculto");
                        return;
                    }
                    cont.classList.remove("oculto");
                    cont.innerHTML = "Cargando partidos...";
                    const detalle = await apiFetch(`/jornadas/${j.id}`);
                    cont.innerHTML = "";
                    detalle.partidos.forEach(p => cont.appendChild(renderPartido(p, usuario)));
                });

                listaEl.appendChild(card);
            });
        } catch (err) {
            listaEl.innerHTML = `<p class="error-msg">${err.message}</p>`;
        }
    }

    function formatearEstado(estado) {
        return estado.replace("_", " ");
    }

    function renderPartido(p, usuario) {
        const div = document.createElement("div");
        div.className = "partido-item";
        div.onclick = (e) => e.stopPropagation();

        const local1 = p.jugadorLocal1 ? `${p.jugadorLocal1.nombre} ${p.jugadorLocal1.apellidos}` : "-";
        const local2 = p.jugadorLocal2 ? `${p.jugadorLocal2.nombre} ${p.jugadorLocal2.apellidos}` : "-";
        const visitante1 = p.parejaVisitante1 || "-";
        const visitante2 = p.parejaVisitante2 || "-";

        div.innerHTML = `
            <div class="cabecera-jornada">
                <strong>Partido ${p.numeroPartido}</strong>
                <span class="etiqueta ${p.estado.toLowerCase()}">${p.estado.replace("_", " ")}</span>
            </div>
            <p>${local1} / ${local2} <strong>vs</strong> ${visitante1} / ${visitante2}</p>
            <p>${p.resultado ? "Resultado: " + p.resultado : "Sin resultado"}</p>
        `;

        if (usuario.rol === "CAPITAN") {
            const btnVisitante = document.createElement("button");
            btnVisitante.className = "btn secundario";
            btnVisitante.textContent = "Añadir pareja visitante";
            btnVisitante.onclick = async () => {
                const v1 = prompt("Nombre visitante 1:", visitante1 !== "-" ? visitante1 : "");
                const v2 = prompt("Nombre visitante 2:", visitante2 !== "-" ? visitante2 : "");
                if (v1 === null || v2 === null) return;
                try {
                    await apiFetch(`/partidos/${p.id}/visitantes`, { method: "PUT", body: { parejaVisitante1: v1, parejaVisitante2: v2 } });
                    location.reload();
                } catch (err) { alert(err.message); }
            };

            const btnResultado = document.createElement("button");
            btnResultado.className = "btn acento";
            btnResultado.textContent = "Añadir resultado";
            btnResultado.onclick = async () => {
                const resultado = prompt("Resultado (ej: 6-3 6-4):", p.resultado || "");
                if (resultado === null) return;
                const ganado = confirm("¿Hemos ganado el partido? Aceptar = Sí, Cancelar = No");
                try {
                    await apiFetch(`/partidos/${p.id}/resultado`, { method: "PUT", body: { resultado, ganado } });
                    location.reload();
                } catch (err) { alert(err.message); }
            };

            const btnNP = document.createElement("button");
            btnNP.className = "btn peligro";
            btnNP.textContent = "Marcar N.P.";
            btnNP.onclick = async () => {
                if (!confirm("¿Marcar este partido como No Presentado (ganado por incomparecencia)?")) return;
                try {
                    await apiFetch(`/partidos/${p.id}/no-presentado`, { method: "PUT" });
                    location.reload();
                } catch (err) { alert(err.message); }
            };

            const acciones = document.createElement("div");
            acciones.className = "acciones-fila";
            acciones.style.marginTop = "8px";
            acciones.append(btnVisitante, btnResultado, btnNP);
            div.appendChild(acciones);
        }

        return div;
    }

    cargarJornadas();
});
