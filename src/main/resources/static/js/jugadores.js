/**
 * jugadores.js - Listado, alta, edición y borrado de jugadores (solo capitán)
 *
 * ============================================================================
 *  RESUMEN DE BUGS CORREGIDOS EN ESTE ARCHIVO (respecto al original)
 * ============================================================================
 *  1) `modalCrearInsance = new bootstrap.Modal(...)` (línea original ~2351)
 *     Faltaba la "t": la variable declarada arriba es "modalCrearInstance",
 *     así que el objeto Modal de Bootstrap nunca se guardaba donde el resto
 *     del código lo buscaba (se quedaba siempre en null). Efecto: al crear
 *     un jugador, el modal no se cerraba solo.
 *
 *  2) `btnNuevoJugador.class.add(d - none)` (línea original ~2360)
 *     "class" no es una propiedad válida en el DOM (es "classList"), y
 *     "d - none" intentaba RESTAR dos variables inexistentes ("d" menos
 *     "none"). Esto lanzaba un ReferenceError en cuanto un jugador NO
 *     capitán abría esta pantalla.
 *
 *  3) `div.querySelector(".btn-borrar")-addEventListener(...)` (línea ~2439)
 *     Un guion en vez de un punto. JavaScript interpretaba esto como una
 *     resta: "(elemento) menos (addEventListener(...))", y como
 *     "addEventListener" suelto no existe como variable global, lanzaba un
 *     ReferenceError. Como esto ocurre DENTRO del try/catch que pinta la
 *     lista, el error hacía caer TODA la lista de jugadores al bloque catch
 *     en cuanto el usuario era capitán (mostraba el mensaje de error en vez
 *     de la plantilla).
 *
 *  4) `document.getElementById("modaDetalleJugador")` (línea ~2353)
 *     Falta una "l": el modal en el HTML se llama "modalDetalleJugador".
 *     Como el elemento nunca se encontraba, el modal de "Ver jugador" nunca
 *     llegaba a inicializarse ni a mostrarse.
 *
 *  5) `deshabilitarModificaciones()` (llamada, línea ~2513) vs
 *     `function deshabilitarModificacion()` (definición, línea ~2544):
 *     el nombre de la llamada no coincide con el de la función (con "es" al
 *     final vs sin ella). Esto lanzaba un ReferenceError al abrir el modal
 *     de detalle, así que el modal JAMÁS llegaba a mostrarse (el error
 *     ocurre antes de la línea que hace show()).
 *
 *  6) `document.getElementById("btnHabilidarEdicion")` (línea ~2538) vs el
 *     id real del botón, "btnHabilitarEdicion" (con "t"): al pulsar
 *     "Habilitar edición" dentro del modal de detalle, esto devolvía null y
 *     el siguiente ".classList.add(...)" sobre null rompía el flujo de
 *     edición de un jugador ya existente.
 *
 *  7) `telefono: document.getElementById("editEmail").value.trim()` en
 *     guardarCambiosJugador (línea ~2568): copiaba el EMAIL dentro del
 *     campo TELÉFONO, y el email real nunca se enviaba al backend. Bug de
 *     copia/pega: cada vez que se editaba un jugador, su email se
 *     sobrescribía silenciosamente con el valor de teléfono.
 *
 *  8) `listaEl.innerHTML;` (línea ~2404) no hace nada (lee la propiedad y la
 *     descarta): no limpiaba la lista antes de volver a pintarla, así que
 *     tras crear/editar un jugador aparecían tarjetas duplicadas.
 *
 * Además, en jugadores.html el botón "Añadir Jugador" tenía:
 *   data-bs-toogle="modal" data-bs-target="modalNuevoJugador"
 * con el atributo "toggle" mal escrito ("toogle") y el target sin la "#" y
 * apuntando a un id que no existe ("modalNuevoJugador" en vez del id real
 * "modalCrearJugador"). Como Bootstrap nunca reconocía ese botón como
 * disparador de modal, el clic no hacía NADA. Ese fix va en jugadores.html
 * (te indico el cambio exacto en mi respuesta).
 * ============================================================================
 */

let modalCrearInstance = null;
let modalDetalleInstance = null;

document.addEventListener("DOMContentLoaded", () => {
    exigirSesion();

    const usuario = getUsuarioActual();
    const esCapitan = usuario && usuario.rol === "CAPITAN";

    // Inicializar modales de Bootstrap 5
    const elModalCrear = document.getElementById("modalCrearJugador");
    if (elModalCrear) modalCrearInstance = new bootstrap.Modal(elModalCrear);

    // FIX (bug 4): el id real en el HTML es "modalDetalleJugador", no "modaDetalleJugador".
    const elModalDetalle = document.getElementById("modalDetalleJugador");
    if (elModalDetalle) modalDetalleInstance = new bootstrap.Modal(elModalDetalle);

    // Ajustes visuales según ROL
    const btnNuevoJugador = document.getElementById("btnNuevoJugador");
    if (btnNuevoJugador) {
        if (!esCapitan) {
            // FIX (bug 2): classList.add(), y el nombre de la clase como string.
            btnNuevoJugador.classList.add("d-none");
        } else {
            btnNuevoJugador.addEventListener("click", () => {
                document.getElementById("formCrearJugador").reset();
                document.getElementById("passPreview").textContent = "12342222-F";
            });
        }
    }

    // Listener para el formulario de Crear jugador
    const formCrear = document.getElementById("formCrearJugador");
    if (formCrear) {
        formCrear.addEventListener("submit", crearJugador);
    }

    // Cargar la lista inicial
    cargarJugadores();
});

/**
 * Calcula la edad a partir de la fecha de nacimiento (AAAA-MM-DD)
 */
function calcularEdad(fechaNacimientoStr) {
    if (!fechaNacimientoStr) return "-";
    const hoy = new Date();
    const nacimiento = new Date(fechaNacimientoStr);
    let edad = hoy.getFullYear() - nacimiento.getFullYear();
    const m = hoy.getMonth() - nacimiento.getMonth();
    if (m < 0 || (m === 0 && hoy.getDate() < nacimiento.getDate())) {
        edad--;
    }
    return edad;
}

/**
 * Carga todos los usuarios del backend y los pinta en el DOM
 */
async function cargarJugadores() {
    const listaEl = document.getElementById("listaJugadores");
    if (!listaEl) return;

    try {
        const jugadores = await apiFetch("/usuarios");
        listaEl.innerHTML = ""; // FIX (bug 8): limpiar antes de repintar, si no, se duplican tarjetas.

        if (!jugadores || jugadores.length === 0) {
            listaEl.innerHTML = '<p class="text-white text-center">No hay jugadores registrados.</p>';
            return;
        }

        // Ordenar por fecha de nacimiento (mayor a menor edad)
        jugadores.sort((a, b) => {
            if (!a.fechaNacimiento) return 1;
            if (!b.fechaNacimiento) return -1;
            return new Date(a.fechaNacimiento) - new Date(b.fechaNacimiento);
        });

        const usuarioActual = getUsuarioActual();
        const esCapitan = usuarioActual && usuarioActual.rol === "CAPITAN";

        jugadores.forEach(j => {
            const edad = j.fechaNacimiento ? calcularEdad(j.fechaNacimiento) : (j.edad || "-");
            const div = document.createElement("div");
            div.className = "card mb-2 bg-dark text-white p-3 border-secondary";
            div.innerHTML = `
            <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h5 class="m-0 font-cinzel">${j.nombre} ${j.apellidos || ''}</h5>
                        <small class="text-muted">${j.posicion || 'Sin Posición'} · ${edad} años · ${j.telefono || 'Sin tel.'}</small>
                    </div>
                    <div class="d-flex gap-2">
                        <button class="btn btn-sm btn-outline-light btn-ver"><i class="bi bi-eye"></i> Ver</button>
                        ${esCapitan ? `<button class="btn btn-sm btn-outline-danger btn-borrar"><i class="bi bi-trash"></i></button>` : ''}
                    </div>
                </div>
                `;
            div.querySelector(".btn-ver").addEventListener("click", () => abrirModalDetalle(j));
            if (esCapitan) {
                // FIX (bug 3): punto en vez de guion.
                div.querySelector(".btn-borrar").addEventListener("click", () => borrarJugador(j.id));
            }

            listaEl.appendChild(div);
        });
    } catch (err) {
        listaEl.innerHTML = `<p class="text-danger text-center">${err.message}</p>`;
    }
}

// Previsualiza el DNI en la alerta de la contraseña por defecto
window.actualizarPassPreview = function () {
    const dniInput = document.getElementById("nuevoDni");
    const preview = document.getElementById("passPreview");
    if (dniInput && preview) {
        preview.textContent = dniInput.value.trim() || "12342222-F";
    }
};

/**
 * Envía el formulario para crear un nuevo usuario al backend.
 * La contraseña inicial es el propio DNI (el backend la cifra y marca
 * "cambioPasswordPendiente" para forzar el cambio en el primer login).
 */
async function crearJugador(e) {
    e.preventDefault();

    const nombreCompleto = document.getElementById("nuevoNombre").value.trim().split(" ");
    const nombre = nombreCompleto[0] || "";
    const apellidos = nombreCompleto.slice(1).join(" ") || " ";
    const dniVal = document.getElementById("nuevoDni").value.trim();

    // Convertir edad introducida en una fecha de nacimiento aproximada
    // (necesario porque el backend guarda un LocalDate, no un número de años)
    const edadVal = parseInt(document.getElementById("nuevaEdad").value, 10);
    const anioNacimiento = new Date().getFullYear() - edadVal;
    const fechaNacimientoAprox = `${anioNacimiento}-01-01`;

    // FIX: ya no se manda "rol" — el backend lo asigna siempre como
    // JUGADOR y ya no acepta ese campo desde el cliente (por seguridad).
    const payload = {
        username: document.getElementById("nuevoUsername").value.trim(),
        password: dniVal, // Contraseña por defecto
        nombre: nombre,
        apellidos: apellidos,
        dni: dniVal,
        fechaNacimiento: fechaNacimientoAprox,
        posicion: document.getElementById("nuevaPosicion").value.toUpperCase(),
        telefono: document.getElementById("nuevoTelefono").value.trim(),
        email: document.getElementById("nuevoEmail").value.trim()
    };

    try {
        await apiFetch("/usuarios", { method: "POST", body: payload });
        if (modalCrearInstance) modalCrearInstance.hide();
        cargarJugadores();
    } catch (err) {
        alert("Error al crear jugador: " + err.message);
    }
}

/**
 * Abre el modal de detalle e inyecta los datos del usuario seleccionado
 */
function abrirModalDetalle(j) {
    document.getElementById("editJugadorId").value = j.id;
    document.getElementById("editNombre").value = `${j.nombre} ${j.apellidos || ''}`;
    document.getElementById("editUsername").value = j.username || "-";
    document.getElementById("editDni").value = j.dni || "";
    document.getElementById("editEdad").value = j.fechaNacimiento ? calcularEdad(j.fechaNacimiento) : "";
    // FIX: el <option value="..."> del select ahora es el mismo texto que
    // manda el backend (DRIVE/REVES/AMBOS en mayúsculas), así que se asigna
    // directamente sin transformar mayúsculas/minúsculas.
    document.getElementById("editPosicion").value = j.posicion || "DRIVE";
    document.getElementById("editTelefono").value = j.telefono || "";
    document.getElementById("editEmail").value = j.email || "";

    // Bloquear campos por defecto
    // FIX (bug 5): el nombre real de la función es singular ("Modificacion").
    deshabilitarModificacion();

    const usuarioActual = getUsuarioActual();
    const btnHabilitar = document.getElementById("btnHabilitarEdicion");
    if (btnHabilitar) {
        if (usuarioActual && usuarioActual.rol === "CAPITAN") {
            btnHabilitar.classList.remove("d-none");
        } else {
            btnHabilitar.classList.add("d-none");
        }
    }

    if (modalDetalleInstance) modalDetalleInstance.show();
}

/**
 * Habilita la edición de los inputs en el modal detalle
 */
window.habilitarModificaciones = function () {
    document.querySelectorAll(".edit-field").forEach(el => {
        el.removeAttribute("readonly");
        el.removeAttribute("disabled");
    });
    document.getElementById("btnGuardarEdicion").classList.remove("d-none");
    // FIX (bug 6): el id real del botón es "btnHabilitarEdicion" (con "t").
    document.getElementById("btnHabilitarEdicion").classList.add("d-none");
};

/**
 * Deshabilita los inputs del modal de detalle (estado de solo lectura)
 */
function deshabilitarModificacion() {
    document.querySelectorAll(".edit-field").forEach(el => {
        el.setAttribute("readonly", "true");
        if (el.tagName === "SELECT") el.setAttribute("disabled", "true");
    });
    document.getElementById("btnGuardarEdicion").classList.add("d-none");
}

/**
 * Guarda las modificaciones realizadas a un usuario
 */
window.guardarCambiosJugador = async function (e) {
    e.preventDefault();
    const id = document.getElementById("editJugadorId").value;

    const nombreCompleto = document.getElementById("editNombre").value.trim().split(" ");
    const nombre = nombreCompleto[0] || "";
    const apellidos = nombreCompleto.slice(1).join(" ") || "";

    const payload = {
        nombre: nombre,
        apellidos: apellidos,
        dni: document.getElementById("editDni").value.trim(),
        posicion: document.getElementById("editPosicion").value.toUpperCase(),
        // FIX (bug 7): antes leía "editEmail" y lo guardaba como teléfono,
        // y el email real nunca se enviaba. Ahora cada campo va a su sitio.
        telefono: document.getElementById("editTelefono").value.trim(),
        email: document.getElementById("editEmail").value.trim()
    };
    try {
        await apiFetch(`/usuarios/${id}`, { method: "PUT", body: payload });
        if (modalDetalleInstance) modalDetalleInstance.hide();
        cargarJugadores();
    } catch (err) {
        alert("Error al actualizar usuario: " + err.message);
    }
};

/**
 * Elimina un jugador de la base de datos
 */
async function borrarJugador(id) {
    if (!confirm("¿Seguro que quieres eliminar la inscripción de este jugador?")) return;
    try {
        await apiFetch(`/usuarios/${id}`, { method: "DELETE" });
        cargarJugadores();
    } catch (err) {
        alert("Error al eliminar: " + err.message);
    }
}