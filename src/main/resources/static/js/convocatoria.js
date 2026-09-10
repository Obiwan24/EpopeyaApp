/**
 * convocatoria.js
 * - Lista de jornadas con convocatoria publicada.
 * - Al entrar en una jornada, muestra los 3 partidos con los 2 convocados de cada uno
 *   y una casilla para que cada jugador confirme su propia convocatoria.
 * - El capitán puede publicar/editar la convocatoria (elegir jugadores por partido).
 *
 * ============================================================================
 *  BUGS CORREGIDOS EN ESTE ARCHIVO (respecto al original)
 * ============================================================================
 *  1) El HTML del modal "Modificar Convocatoria" llama a:
 *         onclick="confirmarSustituto()"
 *     pero este archivo definía "window.confirmarSustitucion" (con "ción").
 *     Al no coincidir el nombre, pulsar "Guardar cambios" o "Desconvocar
 *     jugador" lanzaba un ReferenceError y no hacía nada. Se ha renombrado
 *     la función a "confirmarSustituto" para que coincida EXACTAMENTE con
 *     lo que espera el HTML (así no hace falta tocar convocatoria.html).
 *
 *  2) El payload que se mandaba al backend era ambiguo:
 *         if (slot == 1) payload.jugador1Id = nuevoJugadorId || null;
 *         if (slot == 2) payload.jugador2Id = nuevoJugadorId || null;
 *     Si el slot era 2, la clave "jugador1Id" ni siquiera existía en el
 *     JSON, y el backend no podía distinguir con seguridad "no toques este
 *     campo" de "vacíalo". Ahora se manda siempre `{ slot, jugadorId }`
 *     explícito, que es justo lo que espera el nuevo
 *     ConvocatoriaModificarRequest del backend.
 *
 *  3) `console.error(..., err.mesasage)` -> typo, era "err.message".
 * ============================================================================
 */
let modalModificarInstance = null;
let modalNuevaJornadaInstance = null;
let listaUsuariosGlobal = [];

document.addEventListener("DOMContentLoaded", async () => {
    exigirSesion();
    const usuario = getUsuarioActual();
    const btnNuevaJornada = document.getElementById("btnNuevaJornada");

    // Inicializa instancias de Bootstrap modals
    const elModalModificar = document.getElementById("modalModificarJugador");
    if (elModalModificar) modalModificarInstance = new bootstrap.Modal(elModalModificar);

    const elModalNueva = document.getElementById("modalNuevaJornada");
    if (elModalNueva) modalNuevaJornadaInstance = new bootstrap.Modal(elModalNueva);

    // Cargar lista de jugadores para desplegables si es capitán
    if (usuario && usuario.rol === "CAPITAN") {
        if (btnNuevaJornada) btnNuevaJornada.classList.remove("d-none");
        await cargarJugadoresEnModales();
    }

    // Event listener para formulario de crear jornada
    const formNuevaJornada = document.getElementById("formNuevaJornada");
    if (formNuevaJornada) {
        formNuevaJornada.addEventListener("submit", guardarNuevaJornada);
    }

    // Cargar convocatorias desde el backend
    cargarJornadas();
});

// Carga el combo de usuarios en el modal de alta de jornada y en el de modificación
async function cargarJugadoresEnModales() {
    try {
        listaUsuariosGlobal = await apiFetch("/usuarios");
        const combosModal = document.querySelectorAll(".select-jugador-modal, #selectSustituto");

        combosModal.forEach(select => {
            select.innerHTML = '<option value="">-- Seleccionar Jugador --</option>';
            listaUsuariosGlobal.forEach(u => {
                const opt = document.createElement("option");
                opt.value = u.id;
                opt.textContent = `${u.nombre} ${u.apellidos}`;
                select.appendChild(opt);
            });
        });
    } catch (err) {
        console.error("Error al cargar la lista de usuarios:", err.message); // FIX: "err.mesasage" -> "err.message"
    }
}

// Cargar las jornadas y sus convocados
async function cargarJornadas() {
    const listaEl = document.getElementById("listaJornadas");
    if (!listaEl) return;

    try {
        const jornadas = await apiFetch("/jornadas");
        if (!jornadas || jornadas.length === 0) return;

        listaEl.innerHTML = "";
        const usuario = getUsuarioActual();

        for (const j of jornadas) {
            const partidos = await apiFetch(`/convocatorias/jornadas/${j.id}`);

            const card = document.createElement("div");
            card.className = `jornada-card estado-${j.estado ? j.estado.toLowerCase() : 'activa'}`;

            let htmlPartidos = "";
            if (partidos && partidos.length > 0) {
                partidos.forEach(p => {
                    const conv = p.convocatoria || {};
                    const j1 = conv.jugador1;
                    const j2 = conv.jugador2;

                    htmlPartidos += `
                    <div class="match-box">
                        <div class="fw-bold mb-1">
                            <i class="bi bi-calendar3 me-1"></i> Partido ${p.numeroPartido} — ${p.fechaHora ? new Date(p.fechaHora).toLocaleString() : 'Fecha por confirmar'} (${p.lugar || 'Lugar TBD'})
                        </div>
                        <div class="ps-2">
                            <!-- Jugador 1 -->
                            <div class="player-row">
                                <span><i class="bi bi-person"></i> ${j1 ? `${j1.nombre} ${j1.apellidos}` : '<em>Sin asignar</em>'}</span>
                                <div>
                                    ${j1 ? `
                                        <input type="checkbox" class="form-check-input me-1" ${conv.confirmadoJugador1 ? 'checked disabled' : ''} onchange="confirmarConvocatoria('${conv.id}', 1, this)">
                                        <label class="small me-2">${conv.confirmadoJugador1 ? 'Confirmado' : 'Confirmar'}</label>
                                    ` : ''}
                                    ${usuario.rol === 'CAPITAN' ? `
                                        <button class="btn btn-sm btn-outline-secondary py-0 captain-only" onclick="abrirModalModificar('${j1 ? j1.nombre + ' ' + j1.apellidos : 'Sin Asignar'}', '${p.id}', 1, '${j1 ? j1.id : ''}')">
                                            <i class="bi bi-pencil"></i>
                                        </button>
                                    ` : ''}
                                </div>
                            </div>
                            <!-- Jugador 2 -->
                            <div class="player-row">
                                <span><i class="bi bi-person"></i> ${j2 ? `${j2.nombre} ${j2.apellidos}` : '<em>Sin asignar</em>'}</span>
                                <div>
                                    ${j2 ? `
                                        <input type="checkbox" class="form-check-input me-1" ${conv.confirmadoJugador2 ? 'checked disabled' : ''} onchange="confirmarConvocatoria('${conv.id}', 2, this)">
                                        <label class="small me-2">${conv.confirmadoJugador2 ? 'Confirmado' : 'Confirmar'}</label>
                                    ` : ''}
                                    ${usuario.rol === 'CAPITAN' ? `
                                        <button class="btn btn-sm btn-outline-secondary py-0 captain-only" onclick="abrirModalModificar('${j2 ? j2.nombre + ' ' + j2.apellidos : 'Sin Asignar'}', '${p.id}', 2, '${j2 ? j2.id : ''}')">
                                            <i class="bi bi-pencil"></i>
                                        </button>
                                    ` : ''}
                                </div>
                            </div>
                        </div>
                    </div>
                    `;
                });
            }

            card.innerHTML = `
            <div class="jornada-title d-flex justify-content-between align-items-center">
                <span>${j.equipoLocal || 'EPOPEYA'} vs ${j.rival || j.equipoVisitante}</span>
                <span class="badge ${j.estado === 'FINALIZADA' ? 'bg-secondary' : j.estado === 'APLAZADA' ? 'bg-warning text-dark' : 'bg-warning text-dark'}">${j.estado || 'Pendiente'}</span>
            </div>
            ${htmlPartidos}
        `;

            listaEl.appendChild(card);
        }
    } catch (err) {
        console.warn("No se pudieron cargar las jornadas del backend, manteniendo vista estática si existe:", err.message);
    }
}

// Abre el modal para editar/sustituir convocados
window.abrirModalModificar = function (nombreJugador, partidoId, slot, jugadorId) {
    document.getElementById("nombreJugadorModificar").textContent = nombreJugador;
    document.getElementById("modalPartidoId").value = partidoId || "";
    document.getElementById("modalSlot").value = slot || "";

    const select = document.getElementById("selectSustituto");
    if (select) select.value = jugadorId || "";

    if (modalModificarInstance) {
        modalModificarInstance.show();
    }
};

// Confirma la sustitución del jugador.
// FIX (bug 1): renombrada de "confirmarSustitucion" a "confirmarSustituto"
// para que coincida con el onclick="confirmarSustituto()" del HTML.
window.confirmarSustituto = async function () {
    const partidoId = document.getElementById("modalPartidoId").value;
    const slot = document.getElementById("modalSlot").value;
    const nuevoJugadorId = document.getElementById("selectSustituto").value;

    if (!partidoId) {
        alert("Modificación realizada visualmente.");
        if (modalModificarInstance) modalModificarInstance.hide();
        return;
    }

    try {
        // FIX (bug 2): se manda siempre {slot, jugadorId} en vez de
        // jugador1Id/jugador2Id condicionales, para que el backend sepa
        // siempre y sin ambigüedad qué convocado se está tocando.
        const payload = {
            slot: Number(slot),
            jugadorId: nuevoJugadorId || null
        };

        await apiFetch(`/convocatorias/partidos/${partidoId}`, {
            method: "PUT",
            body: payload
        });

        if (modalModificarInstance) modalModificarInstance.hide();
        cargarJornadas();
    } catch (err) {
        alert(err.message);
    }
};

// Desconvocar al jugador actual
window.desconvocarJugador = async function () {
    document.getElementById("selectSustituto").value = "";
    await confirmarSustituto(); // FIX: llamaba a "confirmarSustitucion", que ya no existe con ese nombre.
};

// Confirmación individual de convocatoria por checkbox
window.confirmarConvocatoria = async function (convocatoriaId, slot, checkbox) {
    if (!convocatoriaId) return;
    try {
        await apiFetch(`/convocatorias/${convocatoriaId}/confirmar?slot=${slot}`, { method: "PUT" });
        checkbox.disabled = true;
    } catch (err) {
        checkbox.checked = false;
        alert(err.message);
    }
};

// Guarda la nueva jornada creando sus 3 partidos y asignando convocados directamente
async function guardarNuevaJornada(e) {
    e.preventDefault();

    const payload = {
        numero: document.getElementById("numeroJornada").value,
        equipoLocal: document.getElementById("equipoLocal").value,
        rival: document.getElementById("equipoRival").value,
        partidos: [
            {
                numeroPartido: 1,
                fechaHora: `${document.getElementById("p1-fecha").value}T${document.getElementById("p1-hora").value}`,
                lugar: document.getElementById("p1-lugar").value,
                jugador1Id: document.getElementById("p1-j1").value || null,
                jugador2Id: document.getElementById("p1-j2").value || null
            },
            {
                numeroPartido: 2,
                fechaHora: `${document.getElementById("p2-fecha").value}T${document.getElementById("p2-hora").value}`,
                lugar: document.getElementById("p2-lugar").value,
                jugador1Id: document.getElementById("p2-j1").value || null,
                jugador2Id: document.getElementById("p2-j2").value || null
            },
            {
                numeroPartido: 3,
                fechaHora: `${document.getElementById("p3-fecha").value}T${document.getElementById("p3-hora").value}`,
                lugar: document.getElementById("p3-lugar").value,
                jugador1Id: document.getElementById("p3-j1").value || null,
                jugador2Id: document.getElementById("p3-j2").value || null
            }
        ]
    };

    try {
        await apiFetch("/jornadas", {
            method: "POST",
            body: payload
        });
        if (modalNuevaJornadaInstance) modalNuevaJornadaInstance.hide();
        cargarJornadas();
    } catch (err) {
        alert("Error al guardar la jornada: " + err.message);
    }
}