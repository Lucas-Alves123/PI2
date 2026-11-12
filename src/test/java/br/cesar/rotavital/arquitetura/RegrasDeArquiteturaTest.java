package br.cesar.rotavital.arquitetura;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class RegrasDeArquiteturaTest {

    private static final String BASE = "br.cesar.rotavital";
    private static JavaClasses classes;

    @BeforeAll
    static void importar() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);
    }

    @Test
    void dominioNaoConheceApresentacaoNemDados() {
        noClasses().that().resideInAPackage("..negocio.dominio..")
                .should().dependOnClassesThat().resideInAnyPackage("..apresentacao..", "..dados..", "..aplicacao..")
                .check(classes);
    }

    @Test
    void dominioNaoDependeDoSpring() {
        noClasses().that().resideInAPackage("..negocio.dominio..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .check(classes);
    }

    @Test
    void controllersNaoAcessamRepositorios() {
        noClasses().that().resideInAPackage("..apresentacao..")
                .should().dependOnClassesThat().resideInAPackage("..dados..")
                .check(classes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"estoque", "requisicao", "expedicao", "hospitais", "telemetria", "notificacao", "identidade"})
    void nenhumContextoAcessaOsDadosDeOutro(String contexto) {
        noClasses().that().resideOutsideOfPackage(BASE + "." + contexto + "..")
                .should().dependOnClassesThat().resideInAPackage(BASE + "." + contexto + ".dados..")
                .check(classes);
    }
}

