/**
 * estadisticas.js
 * Muestra las estadísticas de todos los jugadores.
 * Solo lectura para jugadores; el capitán podría editarlas manualmente
 * desde el listado de jugadores si el backend expone ese endpoint.
 */

document.addEventListener("DOMContentLoaded", () => {
    exigirSesion();
    const cuerpo = document.getElementById("cuerpo-estadisticas");

    async function cargar() {
        cuerpo.innerHTML = `<tr><td colspan="7">Cargando...</td></tr>`;
        try {
            const jugadores = await apiFetch("/estadisticas");
            cuerpo.innerHTML = "";

            jugadores.forEach(j => {
                const tr = document.createElement("tr");
                tr.style.borderBottom = "1px solid var(--color-borde)";
                tr.innerHTML = `
                    <td style="padding:8px;">${j.nombre} ${j.apellidos}</td>
                    <td>${j.estadistica.vecesDisponible}</td>
                    <td>${j.estadistica.vecesConvocado}</td>
                    <td>${j.estadistica.partidosGanados}</td>
                    <td>${j.estadistica.partidosPerdidos}</td>
                    <td>${j.estadistica.setsGanados}</td>
                    <td>${j.estadistica.juegosGanados}</td>
                `;
                cuerpo.appendChild(tr);
            });
        } catch (err) {
            cuerpo.innerHTML = `<tr><td colspan="7" class="error-msg">${err.message}</td></tr>`;
        }
    }

    cargar();
});
