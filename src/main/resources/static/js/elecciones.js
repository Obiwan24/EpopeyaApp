/**
 * elecciones.js
 * Encuesta de elección de fecha cuando jugamos como visitantes.
 * El capitán crea la encuesta (jornada, rival, fechas ofrecidas).
 * Los jugadores votan las fechas que les vienen bien (pueden elegir varias).
 * Al cerrar la encuesta, se elige la fecha más votada y desaparece de esta pantalla.
 */

document.addEventListener("DOMContentLoaded", () => {
    exigirSesion();
    const usuario = getUsuarioActual();
    const cont = document.getElementById("contenido-elecciones");

    async function cargar() {
        cont.innerHTML = "Cargando...";
        try {
            const eleccion = await apiFetch("/elecciones-fecha/activa");

            if (!eleccion) {
                cont.innerHTML = `<div class="tarjeta"><p>No hay elección de fechas pendiente.</p>
                    ${usuario.rol === "CAPITAN" ? `<button class="btn" id="btn-crear">+ Crear elección de fechas</button>` : ""}
                </div>`;
                if (usuario.rol === "CAPITAN") {
                    document.getElementById("btn-crear").addEventListener("click", crearEleccion);
                }
                return;
            }

            let html = `<div class="tarjeta">
                <h3>Jornada ${eleccion.jornada.numero} vs ${eleccion.contrincante}</h3>
                <p>Selecciona todas las fechas en las que puedas jugar:</p>`;

            eleccion.opciones.forEach(op => {
                const yaVote = op.votos.some(v => v.jugador.id === usuario.id);
                html += `<div class="lista-item">
                    <span>${new Date(op.fechaHora).toLocaleString()} (${op.votos.length} votos)</span>
                    <button class="btn ${yaVote ? "" : "secundario"}" data-opcion="${op.id}" data-votado="${yaVote}">
                        ${yaVote ? "Votada ✓" : "Votar"}
                    </button>
                </div>`;
            });

            if (usuario.rol === "CAPITAN") {
                html += `<div class="acciones-fila" style="margin-top:12px;">
                    <button class="btn peligro" id="btn-cerrar-eleccion">Cerrar encuesta y confirmar fecha ganadora</button>
                </div>`;
            }

            html += `</div>`;
            cont.innerHTML = html;

            cont.querySelectorAll("[data-opcion]").forEach(btn => {
                btn.addEventListener("click", async () => {
                    try {
                        if (btn.dataset.votado === "true") {
                            await apiFetch(`/elecciones-fecha/opciones/${btn.dataset.opcion}/voto`, { method: "DELETE" });
                        } else {
                            await apiFetch(`/elecciones-fecha/opciones/${btn.dataset.opcion}/voto`, { method: "POST" });
                        }
                        cargar();
                    } catch (err) { alert(err.message); }
                });
            });

            document.getElementById("btn-cerrar-eleccion")?.addEventListener("click", async () => {
                if (!confirm("Se cerrará la encuesta y se fijará la fecha más votada. ¿Continuar?")) return;
                try {
                    await apiFetch(`/elecciones-fecha/${eleccion.id}/cerrar`, { method: "PUT" });
                    cargar();
                } catch (err) { alert(err.message); }
            });

        } catch (err) {
            cont.innerHTML = `<p class="error-msg">${err.message}</p>`;
        }
    }

    async function crearEleccion() {
        const numeroJornada = prompt("Número de jornada:");
        const contrincante = prompt("Equipo contrincante:");
        if (!numeroJornada || !contrincante) return;

        const fechas = [];
        let seguir = true;
        while (seguir) {
            const f = prompt(`Fecha propuesta #${fechas.length + 1} (AAAA-MM-DDTHH:MM), deja vacío para terminar:`);
            if (!f) { seguir = false; } else { fechas.push(f); }
        }
        if (fechas.length === 0) return;

        try {
            await apiFetch("/elecciones-fecha", {
                method: "POST",
                body: { numeroJornada: Number(numeroJornada), contrincante, fechas }
            });
            cargar();
        } catch (err) { alert(err.message); }
    }

    cargar();
});
