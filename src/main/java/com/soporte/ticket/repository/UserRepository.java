package com.soporte.ticket.repository;



import com.soporte.ticket.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Usuario, Long> {

    // Método útil para buscar usuarios por su correo/username en el login
    Optional<Usuario> findByEmail(String email);
}
