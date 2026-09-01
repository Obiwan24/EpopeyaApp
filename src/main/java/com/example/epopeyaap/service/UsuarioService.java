package com.example.epopeyaap.service;

import com.example.epopeyaap.model.entity.Usuario;
import com.example.epopeyaap.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario crearUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Usuario modificarUsuario(Long id, Usuario nuevosDatos) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario con el id " + id + " no encontrado."));
        usuario.setNombre(nuevosDatos.getNombre());
        usuario.setDni(nuevosDatos.getDni());
        usuario.setEmail(nuevosDatos.getEmail());
        usuario.setTlfn(nuevosDatos.getTlfn());
        usuario.setPosicion(nuevosDatos.getPosicion());
        return usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}
