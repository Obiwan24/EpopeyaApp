/**
 * convocatoria.js
 * - Lista de jornadas con convocatoria publicada.
 * - Al entrar en una jornada, muestra los 3 partidos con los 2 convocados de cada uno
 *   y una casilla para que cada jugador confirme su propia convocatoria.
 * - El capitán puede publicar/editar la convocatoria (elegir jugadores por partido).
 */

document.addEventListener("DOMContentLoaded", () => {
    exigirSesion();
    const usuario = getUsuarioActual();
    const listaEl = document.getElementById("lista-convocatoria");

    if (usuario.rol === "CAPITAN") {
        document.getElementById("btn-nueva-jornada").classList.remove("oculto");
        document.getElementById("btn-nueva-jornada").addEventListener("click", () => {
            irA("jornadas.html");
        });
    }

    async function cargarLista() {
        listaEl.innerHTML = "Cargando...";
        try {
            const jornadas = await apiFetch("/jornadas");
            listaEl.innerHTML = "";

            jornadas.forEach(j => {
                const div = document.createElement("div");
                div.className = "tarjeta lista-item";
                div.style.cursor = "pointer";
                div.innerHTML = `
                    <div><strong>Jornada ${j.numero} - vs ${j.rival}</strong></div>
                    ${usuario.rol === "CAPITAN" ? `
                    <div class="acciones-fila">
                        <button class="btn secundario btn-editar">Editar convocatoria</button>
                    </div>` : ""}
                `;
                if (usuario.rol === "CAPITAN") {
                    div.querySelector(".btn-editar").addEventListener("click", (e) => {
                        e.stopPropagation();
                        mostrarDetalle(j);
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
        const cont = document.getElementById("detalle-convocatoria");
        cont.classList.remove("oculto");
        cont.innerHTML = "Cargando...";

        try {
            const partidos = await apiFetch(`/convocatorias/jornadas/${jornada.id}`);
            let jugadoresEquipo = [];
            if (usuario.rol === "CAPITAN") {
                jugadoresEquipo = await apiFetch("/usuarios");
            }

            let html = `<div class="tarjeta"><h3>Jornada ${jornada.numero} vs ${jornada.rival}</h3>`;

            partidos.forEach(p => {
                const conv = p.convocatoria;
                html += `<div class="partido-item">
                    <strong>Partido ${p.numeroPartido}</strong> - ${p.lugar || "Lugar por confirmar"} - ${p.fechaHora ? new Date(p.fechaHora).toLocaleString() : "Fecha por confirmar"}`;

                if (usuario.rol === "CAPITAN") {
                    html += `<div style="margin-top:8px;">
                        <label>Jugador 1</label>
                        <select data-partido="${p.id}" data-slot="1" class="select-convocado">
                            <option value="">-- Sin convocar --</option>
                            ${jugadoresEquipo.map(j => `<option value="${j.id}" ${conv && conv.jugador1 && conv.jugador1.id === j.id ? "selected" : ""}>${j.nombre} ${j.apellidos}</option>`).join("")}
                        </select>
                        <label>Jugador 2</label>
                        <select data-partido="${p.id}" data-slot="2" class="select-convocado">
                            <option value="">-- Sin convocar --</option>
                            ${jugadoresEquipo.map(j => `<option value="${j.id}" ${conv && conv.jugador2 && conv.jugador2.id === j.id ? "selected" : ""}>${j.nombre} ${j.apellidos}</option>`).join("")}
                        </select>
                        <button class="btn acento" data-guardar="${p.id}" style="margin-top:8px;">Guardar convocatoria</button>
                    </div>`;
                } else if (conv) {
                    const soyJ1 = conv.jugador1 && conv.jugador1.id === usuario.id;
                    const soyJ2 = conv.jugador2 && conv.jugador2.id === usuario.id;
                    html += `<p style="margin-top:8px;">
                        ${conv.jugador1 ? `☑ ${conv.jugador1.nombre} ${conv.jugador1.apellidos} ${conv.confirmadoJugador1 ? "(confirmado)" : "(pendiente)"}` : "Sin asignar"}<br>
                        ${conv.jugador2 ? `☑ ${conv.jugador2.nombre} ${conv.jugador2.apellidos} ${conv.confirmadoJugador2 ? "(confirmado)" : "(pendiente)"}` : "Sin asignar"}
                    </p>`;
                    if (soyJ1 && !conv.confirmadoJugador1) {
                        html += `<button class="btn" data-confirmar="${conv.id}" data-slot="1">Confirmar mi convocatoria</button>`;
                    }
                    if (soyJ2 && !conv.confirmadoJugador2) {
                        html += `<button class="btn" data-confirmar="${conv.id}" data-slot="2">Confirmar mi convocatoria</button>`;
                    }
                } else {
                    html += `<p>Convocatoria aún no publicada</p>`;
                }

                html += `</div>`;
            });
            html += `</div>`;
            cont.innerHTML = html;

            cont.querySelectorAll("[data-guardar]").forEach(btn => {
                btn.addEventListener("click", async () => {
                    const partidoId = btn.dataset.guardar;
                    const sel1 = cont.querySelector(`select[data-partido="${partidoId}"][data-slot="1"]`).value;
                    const sel2 = cont.querySelector(`select[data-partido="${partidoId}"][data-slot="2"]`).value;
                    try {
                        await apiFetch(`/convocatorias/partidos/${partidoId}`, {
                            method: "PUT",
                            body: { jugador1Id: sel1 || null, jugador2Id: sel2 || null }
                        });
                        mostrarDetalle(jornada);
                    } catch (err) { alert(err.message); }
                });
            });

            cont.querySelectorAll("[data-confirmar]").forEach(btn => {
                btn.addEventListener("click", async () => {
                    try {
                        await apiFetch(`/convocatorias/${btn.dataset.confirmar}/confirmar?slot=${btn.dataset.slot}`, { method: "PUT" });
                        mostrarDetalle(jornada);
                    } catch (err) { alert(err.message); }
                });
            });

        } catch (err) {
            cont.innerHTML = `<p class="error-msg">${err.message}</p>`;
        }
    }

    cargarLista();
});
