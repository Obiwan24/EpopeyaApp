/**
 * disponibilidad.js
 * - Jugador: ve las jornadas con encuesta abierta y marca Sí/No disponible.
 * - Capitán: además puede crear jornadas con encuesta y ver/editar la disponibilidad de todo el equipo.
 */

document.addEventListener("DOMContentLoaded", () => {
    exigirSesion();
    const usuario = getUsuarioActual();
    const listaEl = document.getElementById("lista-disponibilidad");

    if (usuario.rol === "CAPITAN") {
        document.getElementById("btn-nueva-jornada").classList.remove("oculto");
        document.getElementById("btn-nueva-jornada").addEventListener("click", crearJornadaConEncuesta);
    }

    async function crearJornadaConEncuesta() {
        const numero = prompt("Número de jornada:");
        const rival = prompt("Rival:");
        const fechaLimite = prompt("Fecha límite para responder la encuesta (AAAA-MM-DDTHH:MM):");
        if (!numero || !rival || !fechaLimite) return;
        try {
            await apiFetch("/jornadas", { method: "POST", body: { numero: Number(numero), rival, fechaLimiteEncuesta: fechaLimite } });
            cargarLista();
        } catch (err) { alert(err.message); }
    }

    async function cargarLista() {
        listaEl.innerHTML = "Cargando...";
        try {
            const jornadas = await apiFetch("/disponibilidad/jornadas");
            listaEl.innerHTML = "";

            jornadas.forEach(j => {
                const div = document.createElement("div");
                div.className = "tarjeta lista-item";
                div.style.cursor = "pointer";
                div.innerHTML = `
                    <div>
                        <strong>Jornada ${j.numero} - vs ${j.rival}</strong><br>
                        <small>Cierra: ${new Date(j.fechaLimiteEncuesta).toLocaleString()}</small>
                    </div>
                    ${usuario.rol === "CAPITAN" ? `
                    <div class="acciones-fila">
                        <button class="btn secundario btn-editar-jornada">Editar</button>
                        <button class="btn peligro btn-borrar-jornada">Eliminar</button>
                    </div>` : ""}
                `;

                if (usuario.rol === "CAPITAN") {
                    div.querySelector(".btn-borrar-jornada").addEventListener("click", async (e) => {
                        e.stopPropagation();
                        if (!confirm("¿Eliminar esta jornada?")) return;
                        await apiFetch(`/jornadas/${j.id}`, { method: "DELETE" });
                        cargarLista();
                    });
                    div.querySelector(".btn-editar-jornada").addEventListener("click", (e) => {
                        e.stopPropagation();
                        alert("Edición de jornada: usa la pantalla de Jornadas para más detalle.");
                    });
                }

                div.addEventListener("click", () => mostrarDetalle(j));
                listaEl.appendChild(div);
            });
        } catch (err) {
            listaEl.innerHTML = `<p class="error-msg">${err.message}</p>`;
        }
    }

    async function mostrarDetalle(jornada) {
        const cont = document.getElementById("detalle-disponibilidad");
        cont.classList.remove("oculto");
        cont.innerHTML = "Cargando...";

        try {
            const datos = await apiFetch(`/disponibilidad/jornadas/${jornada.id}`);
            // datos = { jornada, respuestas: [{jugador, disponible, fechaRespuesta, bloqueada}] }

            let html = `<div class="tarjeta"><h3>Jornada ${datos.jornada.numero} vs ${datos.jornada.rival}</h3>`;
            html += `<p>Encuesta cierra: ${new Date(datos.jornada.fechaLimiteEncuesta).toLocaleString()}</p>`;

            datos.respuestas.forEach(r => {
                const soyYo = r.jugador.id === usuario.id;
                const puedoEditar = (soyYo || usuario.rol === "CAPITAN") && !r.bloqueada;
                html += `<div class="lista-item">
                    <div>${r.jugador.nombre} ${r.jugador.apellidos} (${calcularEdad(r.jugador.fechaNacimiento)} años) - ${r.jugador.posicion}</div>
                    <div class="acciones-fila">
                        <button class="btn ${r.disponible === true ? "" : "secundario"}" data-jugador="${r.jugador.id}" data-valor="true" ${!puedoEditar ? "disabled" : ""}>Sí</button>
                        <button class="btn ${r.disponible === false ? "peligro" : "secundario"}" data-jugador="${r.jugador.id}" data-valor="false" ${!puedoEditar ? "disabled" : ""}>No</button>
                    </div>
                </div>`;
            });
            html += `</div>`;
            cont.innerHTML = html;

            cont.querySelectorAll("button[data-jugador]").forEach(btn => {
                btn.addEventListener("click", async () => {
                    const jugadorId = btn.dataset.jugador;
                    const valor = btn.dataset.valor === "true";
                    try {
                        await apiFetch(`/disponibilidad/jornadas/${jornada.id}/jugadores/${jugadorId}`, {
                            method: "PUT",
                            body: { disponible: valor }
                        });
                        mostrarDetalle(jornada);
                    } catch (err) {
                        alert(err.message);
                    }
                });
            });
        } catch (err) {
            cont.innerHTML = `<p class="error-msg">${err.message}</p>`;
        }
    }

    cargarLista();
});
