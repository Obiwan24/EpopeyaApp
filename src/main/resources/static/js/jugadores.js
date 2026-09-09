/**
 * jugadores.js - Listado, alta, edición y borrado de jugadores (solo capitán)
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

    const elModalDetalle = document.getElementById("modaDetalleJugador");
    if (elModalDetalle) modalDetalleInstance = new bootstrap.Modal(elModalDetalle);

// Ajustes visuales segun ROL
    const btnNuevoJugador = document.getElementById("btnNuevoJugador");
    if (btnNuevoJugador) {
        if (!esCapitan) {
            btnNuevoJugador.class.add("d-none");
        } else {
            btnNuevoJugador.addEventListener("click", () => {
                document.getElementById("formCrearJugador").reset();
                document.getElementById("passPreview").textContent = "12342222-F";
            });
        }
    }
    // Listener para el formulario de Crear jugador
    const formCrear = document.getElementById("formCrearJugador");
    if (formCrear){
        formCrear.addEventListener("submit", crearJugador);
    }

// Cargar la lista inicial
    cargarJugadores();
});

/**
 *  Calcula la edad a partir de la fecha de nacimiento (YYY-MM-DD)
 */

function calcularEdad(fechaNacimientoStr){
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
 *  Carga todos los usuarios del backend y los pinta en el DOM
 */

async function cargarJugadores() {
    const listaEl = document.getElementById("listaJugadores");
    if (!listaEl) return;

    try{
        const jugadores= await apiFetch("/usuarios");
        listaEl.innerHTML;

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
            if (esCapitan){
                div.querySelector(".btn-borrar").addEventListener("click", () => borrarJugador(j.id));
            }

            listaEl.appendChild(div)
        });
    } catch (err) {
        listaEl.innerHTML = `<p class="text-danger text-center">${err.message}</p>`;
    }
}

// Previsualiza el DNI en la alerta de la contraseña por defecto
 window.actualizarPassPreview = function (){
    const dniInput = document.getElementById("nuevoDni");
    const preview = document.getElementById("passPreview");
    if (dniInput && preview) {
        preview.textContent = dniInput.value.trim() || "12342222-F";
    }
 };

/**
 * Envia el formulario para crear un nuevo usuario al backend
 */

async function crearJugador(e) {
    e.preventDefault();

    const nombreCompleto = document.getElementById("nuevoNombre").value.trim().split(" ");
    const nombre = nombreCompleto[0] || "";
    const apellidos = nombreCompleto.slice(1).join(" ") || " ";
    const dniVal = document.getElementById("nuevoDni").value.trim();

    // Convertir dad ingresada en una fecha de nacimiento aproximada
    // Necesario para campo LocalDate de Java
    const edadVal = parseInt(document.getElementById("nuevaEdad").value, 10);
    const anioNacimiento = new Date().getFullYear() - edadVal;
    const fechaNacimientoAprox = `${anioNacimiento}-01-01`;

    const payload = {
        username: document.getElementById("nuevoUsername").value.trim(),
        password: dniVal, //Contraseña por defecto
        nombre: nombre,
        apellidos: apellidos,
        dni: dniVal,
        fechaNacimiento: fechaNacimientoAprox,
        posicion: document.getElementById("nuevaPosicion").value.toUpperCase(),
        telefono: document.getElementById("nuevoTelefono").value.trim(),
        email: document.getElementById("nuevoEmail").value.trim(),
        rol: "JUGADOR"
    };

    try{
        await apiFetch("/usuarios", { method: "POST", body: payload});
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
    document.getElementById("editPosicion").value = j.posicion ? (j.posicion.charAt(0) + j.posicion.slice(1).toLowerCase()) : "DRIVE";
    document.getElementById("editTelefono").value = j.telefono || "";
    document.getElementById("editEmail").value = j.email || "";

    // Bloquear campos por defecto
    deshabilitarModificacion();

    const usuarioActual = getUsuarioActual();
    const btnHabilitar = document.getElementById("btnHabilitarEdicion");
    if (btnHabilitar) {
        if (usuarioActual && usuarioActual.rol === "CAPITAN") {
            btnHabilitar.classList.remove("d-none");
        }else{
            btnHabilitar.classList.add("d-none");
        }
    }

    if (modalDetalleInstance) modalDetalleInstance.show();
}

/**
 * Habilita la edicion de los inputs en el modal detalle
 */

window.habilitarModificaciones = function () {
    document.querySelectorAll(".edit-field").forEach(el => {
        el.removeAttribute("readonly");
        el.removeAttribute("disabled");
    });
    document.getElementById("btnGuardarEdicion").classList.remove("d-none");
    document.getElementById("btnHabilidarEdicion").classList.add("d-none");
};

/**
 * Deshabilitar los inputs del modal de detalle
 */
function deshabilitarModificacion(){
    document.querySelectorAll(".edit-field").forEach(el => {
        el.setAttribute("readonly", "true");
        if (el.tagName === "SELECT") el.setAttribute("disabled", "true");
    });
    document.getElementById("btnGuardarEdicion").classList.add("d-none");
}

/**
 * Guarda las modificaciones realizadas a un usuario
 */
window.guardarCambiosJugador = async  function(e) {
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
        telefono: document.getElementById("editEmail").value.trim()
    }
    try {
        await apiFetch(`/usuarios/${id}`, {method: "PUT", body: payload });
        if (modalDetalleInstance) modalDetalleInstance.hide();
        cargarJugadores();
    }catch (err) {
        alert("Error al actualizar usuario: " + err.message);
    }
};

/**
 * Elimina un jugador de la base de datos
 */

async function borrarJugador(id) {
    if (!confirm("¿Seguro que quieres eliminar la inscripción de este jugador?")) return;
    try{
        await apiFetch(`/usuarios/${id}`, {method: "DELETE"});
        cargarJugadores();
    }catch (err){
        alert("Error al eliminar: " + err.message);
    }
}