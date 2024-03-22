package ag.selm.manager;

import ag.selm.manager.entity.SelmagUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SelmanUserRepository extends CrudRepository<SelmagUser, Integer> {

    Optional<SelmagUser> findByUsername(String username);
}
