package net.javaguides.springboot;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.springboot.dto.CompanyOneDto;
import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.Company;
import net.javaguides.springboot.service.CompanyService;
import net.javaguides.springboot.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Slf4j
@Testcontainers
public class CompanyControllerTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    private String token;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(this.context)
                .apply(springSecurity()) // Включаем поддержку Spring Security
                .build();
    }

    @Test
    void contextLoads() {}

    // Company-test-getAll
    @Test
    @WithMockUser
    public void testGetAllCompanies_SuccessfullyCompanies() throws Exception {

        List<Company> companies = getCompanies();

        token = jwtUtil.generateToken("user1", 1L);
        when(companyService.getAllCompanies()).thenReturn(CompletableFuture.completedFuture(companies));
        // Выполнение запроса и проверка результата
        MvcResult result = mockMvc.perform(get("/api/comp-vac/company")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn(); // Получаем результат выполнения запроса

        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);
        // Проверка, что метод findAll был вызван
        verify(companyService, times(1)).getAllCompanies();
    }

    @Test
    @WithMockUser
    public void testGetAllCompanies_EmptyList() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        token = jwtUtil.generateToken("user1", 1L);
        // Выполнение запроса и проверка результата
        MvcResult result = mockMvc.perform(get("/api/comp-vac/company")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn(); // Получаем результат выполнения запроса

        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);
        // Проверка, что метод findAll был вызван
        verify(companyService, times(1)).getAllCompanies();
    }

    @Test
    @WithMockUser
    public void testGetAllCompanies_WithOtherRole() throws Exception {
        List<Company> companies = getCompanies();

        token = jwtUtil.generateToken("user1", 2L);
        when(companyService.getAllCompanies()).thenReturn(CompletableFuture.completedFuture(companies));

        MvcResult result = mockMvc.perform(get("/api/comp-vac/company")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden()) //403
                .andReturn();
        verify(companyService, never()).getAllCompanies();
    }

    @Test
    @WithMockUser
    public void testGetAllCompanies_withoutToken() throws Exception {
        List<Company> companies = getCompanies();

        token = "52";
        when(companyService.getAllCompanies()).thenReturn(CompletableFuture.completedFuture(companies));

        MvcResult result = mockMvc.perform(get("/api/comp-vac/company")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized()) //401 (invalid token)
                .andReturn();
        verify(companyService, never()).getAllCompanies();
    }

    // Company-test-post
    @Test
    @WithMockUser
    public void testPost_SuccessfullyCompany() throws Exception {

        Company company1 = new Company();
        company1 = getCompanies().get(0);
        company1.setCompany_id(52L);


        token = jwtUtil.generateToken("user1", 1L);
        when(companyService.createCompany(any(Company.class))).thenReturn(CompletableFuture.completedFuture(company1));
        // Выполнение запроса и проверка результата
        MvcResult result = mockMvc.perform(post("/api/comp-vac/company")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(company1)))
                .andExpect(status().isOk())
                .andReturn(); // Получаем результат выполнения запроса

        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);

        verify(companyService, times(1)).createCompany(any(Company.class));
    }

    @Test
    @WithMockUser
    public void testPost_CompanyBadRequest() throws Exception {

        Company company1 = new Company();
        company1 = getCompanies().get(0);
        company1.setCompany_id(52L);


        token = jwtUtil.generateToken("user1", 1L);
        when(companyService.createCompany(null)).thenReturn(CompletableFuture.completedFuture(company1));
        // Выполнение запроса и проверка результата
        MvcResult result = mockMvc.perform(post("/api/comp-vac/company")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(company1)))
                .andExpect(status().isBadRequest()) //400
                .andReturn(); // Получаем результат выполнения запроса

        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);

        verify(companyService, times(0)).createCompany(null);
    }

    @Test
    @WithMockUser
    public void testPost_WithOtherRole() throws Exception {

        Company company1 = new Company();
        company1 = getCompanies().get(0);
        company1.setCompany_id(52L);


        token = jwtUtil.generateToken("user1", 2L);
        when(companyService.createCompany(any(Company.class))).thenReturn(CompletableFuture.completedFuture(company1));
        // Выполнение запроса и проверка результата
        MvcResult result = mockMvc.perform(post("/api/comp-vac/company")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(company1)))
                .andExpect(status().isForbidden()) //403
                .andReturn(); // Получаем результат выполнения запроса

        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);

        verify(companyService, times(0)).createCompany(company1);
    }

    // Company-test-getOne
    @Test
    @WithMockUser
    public void testGetOneCompany_SuccessfullyCompany() throws Exception {

        Long id = 52L;
        token = jwtUtil.generateToken("user1", 1L);
        CompanyOneDto companyOneDto = new CompanyOneDto();
        companyOneDto.setCompany_id(id);
        when(companyService.getOneCompany(id)).thenReturn(CompletableFuture.completedFuture(companyOneDto));
        // Выполнение запроса и проверка результата
        MvcResult result = mockMvc.perform(get("/api/comp-vac/company/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn(); // Получаем результат выполнения запроса

        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);

        verify(companyService, times(1)).getOneCompany(id);
    }

    @Test
    @WithMockUser
    public void testGetOneCompany_CompanyNotFound() throws Exception {

        Long id = 52L;
        token = jwtUtil.generateToken("user1", 1L);

        when(companyService.getOneCompany(id)).thenThrow(new ResourceNotFoundException("Компании с id: " + id + " не существует!"));
        // Выполнение запроса и проверка результата
        MvcResult result = mockMvc.perform(get("/api/comp-vac/company/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andReturn(); // Получаем результат выполнения запроса

        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);

        verify(companyService, times(1)).getOneCompany(id);
    }

    // Company-test-update
    @Test
    @WithMockUser
    public void testUpdate_SuccessfullyCompany() throws Exception {

        Company company1 = getCompanies().get(0);
        Long id = 52L;
        company1.setCompany_id(id);

        token = jwtUtil.generateToken("user1", 1L);

        when(companyService.updateCompany(anyLong(), any(Company.class))).thenReturn(CompletableFuture.completedFuture(company1));
        // Выполнение запроса и проверка результата
        MvcResult result = mockMvc.perform(put("/api/comp-vac/company/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(company1)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);

        verify(companyService, times(1)).updateCompany(anyLong(), any(Company.class));
    }

    @Test
    @WithMockUser
    public void testUpdate_CompanyNotFound() throws Exception {

        Company company1 = getCompanies().get(0);
        company1.setCompany_id(anyLong());
        Long id = 52L;

        token = jwtUtil.generateToken("user1", 1L);

        when(companyService.updateCompany(id, any(Company.class))).thenThrow(new ResourceNotFoundException("Компании с id: " + id + " не существует!"));
        // Выполнение запроса и проверка результата
        MvcResult result = mockMvc.perform(put("/api/comp-vac/company/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(company1)))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);

        verify(companyService, never()).updateCompany(id, company1);
    }

    @Test
    @WithMockUser
    public void testUpdate_CompanyBadRequest() throws Exception {

        Company company1 = new Company();
        Long id = 52L;

        token = jwtUtil.generateToken("user1", 1L);

        when(companyService.updateCompany(id, company1)).thenThrow(new ResourceNotFoundException("Компании с id: " + id + " не существует!"));
        // Выполнение запроса и проверка результата
        MvcResult result = mockMvc.perform(put("/api/comp-vac/company/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(company1)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);

        verify(companyService, never()).updateCompany(id, company1);
    }

    // Company-test-delete
    @Test
    @WithMockUser
    public void testDelete_SuccessfullyCompany() throws Exception {

        Long id = 52L;
        token = jwtUtil.generateToken("user1", 1L);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Компания успешно удалена!");

        when(companyService.deleteCompany(id)).thenReturn(CompletableFuture.completedFuture(ResponseEntity.ok(response).getBody()));
        // Выполнение запроса и проверка результата
        MvcResult result = mockMvc.perform(delete("/api/comp-vac/company/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn(); // Получаем результат выполнения запроса

        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);

        verify(companyService, times(1)).deleteCompany(id);
    }

    @Test
    @WithMockUser
    public void testDelete_CompanyNotFound() throws Exception {

        Long id = 52L;
        token = jwtUtil.generateToken("user1", 1L);

        when(companyService.deleteCompany(id)).thenThrow(new ResourceNotFoundException("Компании с id: " + id + " не существует!"));
        // Выполнение запроса и проверка результата
        MvcResult result = mockMvc.perform(delete("/api/comp-vac/company/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn(); // Получаем результат выполнения запроса

        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);

        verify(companyService, times(1)).deleteCompany(id);
    }

    @Test
    @WithMockUser
    public void testDelete_WithOtherRole() throws Exception {

        Long id = 52L;
        token = jwtUtil.generateToken("user1", 2L);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Компания успешно удалена!");

        when(companyService.deleteCompany(id)).thenReturn(CompletableFuture.completedFuture(ResponseEntity.ok(response).getBody()));
        // Выполнение запроса и проверка результата
        MvcResult result = mockMvc.perform(delete("/api/comp-vac/company/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andReturn(); // Получаем результат выполнения запроса

        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);

        verify(companyService, never()).deleteCompany(id);
    }

    private static List<Company> getCompanies() throws ParseException {
        String name = "52";
        String inn = "25555";
        String kpp = "25555";
        String ogrn = "25555";
        String address = "25555";
        String director = "25555";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date date_reg = sdf.parse("2024-10-01T00:00:00.000+00:00");
        boolean isAccepted = false;
        Company company1 = new Company(name, inn, kpp, ogrn, address, director, date_reg, isAccepted);
        Company company2 = new Company(name, "52", kpp, ogrn, address, director, date_reg, isAccepted);
        return Arrays.asList(company1, company2);
    }
}