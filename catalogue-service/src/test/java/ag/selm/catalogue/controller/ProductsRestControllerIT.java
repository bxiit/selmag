package ag.selm.catalogue.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ProductsRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Sql("/ag/selm/catalogue/sql/products.sql")
    @Test
    public void findProducts_ReturnsProductsList() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/catalogue-api/products")
                .param("filter", "товар")
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")));
        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(APPLICATION_JSON),
                        content().json("""
                                [
                                {"id": 1, "title":  "Товар №1", "details": "Описание товара №1"},
                                {"id": 3, "title":  "Товар №3", "details": "Описание товара №3"}
                                ]
                                """)
                );
    }

    @Test
    public void createProduct_RequestIsValid_ReturnsNewProduct() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/products")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                            "title" : "Еще один товар",
                            "details" : "Описание нового товара"
                        }
                        """)
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")));
        // when
        this.mockMvc.perform(requestBuilder)
        // then
                .andDo(print())
                .andExpectAll(
                        status().isCreated(),
                        header().string(LOCATION, "http://localhost/catalogue-api/products/1"),
                        content().contentType(APPLICATION_JSON),
                        content().json("""
                                {
                                    "id" : 1,
                                    "title" : "Еще один товар",
                                    "details" : "Описание нового товара"
                                }
                                        """)
                );
    }

    @Test
    public void createProduct_RequestIsInvalid_ReturnsProblemDetail() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/products")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                            "title" : "  ",
                            "details" : null
                        }
                        """)
                .with(jwt().jwt(builder -> builder.claim("scope", "edit_catalogue")))
                .locale(Locale.of("ru", "RU"));
        // when
        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(APPLICATION_PROBLEM_JSON),
                        content().json("""
                                {
                                "errors": [
                                    "Название товара должно быть от 3 до 50 символов"
                                ]}
                                """)
                );
    }

    @Test
    public void createProduct_UserIsNotAuthorized_ReturnsForbidden() throws Exception {
        // given
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/catalogue-api/products")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                            "title" : "  ",
                            "details" : null
                        }
                        """)
                .with(jwt().jwt(builder -> builder.claim("scope", "view_catalogue")))
                .locale(Locale.of("ru", "RU"));
        // when
        this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpectAll(
                        status().isForbidden()
                );
    }
}