package ag.selm.catalogue.repository;

import ag.selm.catalogue.entity.Product;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends CrudRepository<Product, Integer> {
    // select * from catalogue.t_product where c_title ilike :filter
    Iterable<Product> findAllByTitleLikeIgnoreCase(String filter);
}
