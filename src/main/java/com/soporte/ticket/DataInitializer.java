package  com.soporte.ticket;

import com.soporte.ticket.model.Usuario;
import com.soporte.ticket.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Inyección por constructor
    public DataInitializer(UserRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String @NonNull ... args) throws Exception {

        if (usuarioRepository.count() == 0) {

            // 1. Creamos un Cliente de prueba
            Usuario cliente = new Usuario(
                    null,
                    "cliente@correo.com",
                    passwordEncoder.encode("password123"),
                    "ROLE_CLIENTE" // 🔥 Texto directo
            );
            usuarioRepository.save(cliente);

            // 2. Creamos un Técnico de prueba
            Usuario tecnico = new Usuario(
                    null,
                    "tecnico@correo.com",
                    passwordEncoder.encode("password123"),
                    "ROLE_TECNICO" // 🔥 Texto directo
            );
            usuarioRepository.save(tecnico);

            // 3. Creamos el Administrador de prueba (Crucial para tu prueba de Postman)
            Usuario admin = new Usuario(
                    null,
                    "admin@correo.com",
                    passwordEncoder.encode("password123"),
                    "ROLE_ADMIN" // 🔥 Texto directo
            );
            usuarioRepository.save(admin);

            System.out.println("✅ ¡Usuarios de prueba inicializados con éxito!");
        }
    }
}
