package Controller;

import com.ejemplo.model.Usuario;
import repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Usuario usuario) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findByCorreo(usuario.getCorreo());
        if (usuarioExistente.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(usuarioExistente.get());
        }

        String contraseñaEncriptada = new BCryptPasswordEncoder().encode(usuario.getContraseña());
        usuario.setContraseña(contraseñaEncriptada);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado);
    }
}
