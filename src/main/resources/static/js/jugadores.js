/**
 * jugadores.js - Listado, alta, edición y borrado de jugadores (solo capitán)
 */

document.addEventListener("DOMContentLoaded", () => {
    exigirSesion();

    const usuario = getUsuarioActual();
    if (!usuario || usuario.rol !== "CAPITAN") {
        irA("principal.html");
        return;
    }

    const listaEl = document.getElementById("lista-jugadores");
    const modal = document.getElementById("modal-jugador");
    const form = document.getElementById("form-jugador");
    const errorEl = document.getElementById("error-jugador");
    const tituloModal = document.getElementById("titulo-modal");

    document.getElementById("btn-nuevo").addEventListener("click", () => abrirModal());
    document.getElementById("cancelar-modal").addEventListener("click", () => modal.classList.add("oculto"));

    function abrirModal(jugador = null) {
        form.reset();
        errorEl.textContent = "";
        if (jugador) {
            tituloModal.textContent = "Editar jugador";
            document.getElementById("jugador-id").value = jugador.id;
            document.getElementById("jugador-nombre").value = jugador.nombre;
            document.getElementById("jugador-apellidos").value = jugador.apellidos;
            document.getElementById("jugador-dni").value = jugador.dni;
            document.getElementById("jugador-telefono").value = jugador.telefono || "";
            document.getElementById("jugador-email").value = jugador.email || "";
            document.getElementById("jugador-fecha-nacimiento").value = jugador.fechaNacimiento || "";
            document.getElementById("jugador-posicion").value = jugador.posicion || "DRIVE";
        } else {
            tituloModal.textContent = "Nuevo jugador";
            document.getElementById("jugador-id").value = "";
        }
        modal.classList.remove("oculto");
    }

    async function cargarJugadores() {
        listaEl.innerHTML = "Cargando...";
        try {
            // El backend ya devuelve la lista ordenada por edad (mayor a menor)
            const jugadores = await apiFetch("/usuarios");
            listaEl.innerHTML = "";

            jugadores.forEach(j => {
                const div = document.createElement("div");
                div.className = "tarjeta lista-item";
                div.innerHTML = `
                    <div>
                        <strong>${j.nombre} ${j.apellidos}</strong> (${calcularEdad(j.fechaNacimiento)} años)<br>
                        <small>${j.posicion} · ${j.dni} · ${j.telefono || "-"} · ${j.email || "-"}</small>
                    </div>
                    <div class="acciones-fila">
                        <button class="btn secundario btn-editar">Editar</button>
                        <button class="btn peligro btn-borrar">Eliminar</button>
                    </div>
                `;
                div.querySelector(".btn-editar").addEventListener("click", () => abrirModal(j));
                div.querySelector(".btn-borrar").addEventListener("click", () => borrarJugador(j.id));
                listaEl.appendChild(div);
            });
        } catch (err) {
            listaEl.innerHTML = `<p class="error-msg">${err.message}</p>`;
        }
    }

    async function borrarJugador(id) {
        if (!confirm("¿Seguro que quieres eliminar la inscripción de este jugador?")) return;
        try {
            await apiFetch(`/usuarios/${id}`, { method: "DELETE" });
            cargarJugadores();
        } catch (err) {
            alert(err.message);
        }
    }

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        errorEl.textContent = "";

        const id = document.getElementById("jugador-id").value;
        const payload = {
            nombre: document.getElementById("jugador-nombre").value.trim(),
            apellidos: document.getElementById("jugador-apellidos").value.trim(),
            dni: document.getElementById("jugador-dni").value.trim(),
            telefono: document.getElementById("jugador-telefono").value.trim(),
            email: document.getElementById("jugador-email").value.trim(),
            fechaNacimiento: document.getElementById("jugador-fecha-nacimiento").value,
            posicion: document.getElementById("jugador-posicion").value
        };

        try {
            if (id) {
                await apiFetch(`/usuarios/${id}`, { method: "PUT", body: payload });
            } else {
                await apiFetch("/usuarios", { method: "POST", body: payload });
            }
            modal.classList.add("oculto");
            cargarJugadores();
        } catch (err) {
            errorEl.textContent = err.message;
        }
    });

    cargarJugadores();
});
