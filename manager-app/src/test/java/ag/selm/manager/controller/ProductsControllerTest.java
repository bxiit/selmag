package ag.selm.manager.controller;

import ag.selm.manager.client.BadRequestException;
import ag.selm.manager.client.ProductsRestClient;
import ag.selm.manager.controller.payload.NewProductPayload;
import ag.selm.manager.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты ProductsController")
class ProductsControllerTest {
    @Mock
    ProductsRestClient restClient;
    @InjectMocks
    ProductsController controller;

    @Test
    @DisplayName("createProduct создаст новый товар и перенаправит на страницу товара")
    public void createProduct_RequestIsValid_ReturnsRedirectionToProductPage() {
        // given
        var payload = new NewProductPayload("Новый товар", "Описание нового товара");
        var model = new ConcurrentModel();

        doReturn(new Product(1, "Новый товар", "Описание нового товара"))
                .when(this.restClient)
                .createProduct("Новый товар", "Описание нового товара");

        // when
        var result = this.controller.createProduct(payload, model);

        // then
        assertEquals("redirect:/catalogue/products/1", result);

        verify(this.restClient).createProduct("Новый товар", "Описание нового товара");
        verifyNoMoreInteractions(this.restClient);
    }

    @Test
    @DisplayName("createProduct выбросит исключение BadRequestException и вернет страницу создания товара")
    public void createProduct_RequestIsInvalid_ReturnsCreateProductPageWithErrors() {
        // given
        var payload = new NewProductPayload("as", null);
        var model = new ConcurrentModel();

        doThrow(new BadRequestException(List.of("Ошибка 1", "Ошибка 2")))
                .when(this.restClient)
                .createProduct("as", null);

        // when
        var result = this.controller.createProduct(payload, model);

        // then
        assertEquals("catalogue/products/new_product", result);
        assertEquals(payload, model.getAttribute("payload"));
        assertEquals(List.of("Ошибка 1", "Ошибка 2"), model.getAttribute("errors"));

        verify(this.restClient).createProduct("as", null);
        verifyNoMoreInteractions(this.restClient);
    }
}