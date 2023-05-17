package org.av360.maverick.eventdispatcher.shared;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;


class SharedTest {
    @Test
    void dtoOnlyAccessedByApi() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("org.av360.maverick.eventdispatcher.shared");

        ArchRule rule = classes()
                .that().resideInAPackage("..dto..")
                .should().onlyBeAccessed()
                .byClassesThat().resideInAnyPackage("..dto..", "org.av360.maverick.eventdispatcher.subscriptions.subscription.api..");

        rule.check(importedClasses);
    }

    @Test
    void messageOnlyAccessedByRpc() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("org.av360.maverick.eventdispatcher");

        ArchRule rule = classes()
                .that()
                .haveSimpleName("SubscriptionMessage")
                .should()
                .onlyBeAccessed()
                .byClassesThat()
                .resideInAnyPackage("org.av360.maverick.eventdispatcher.shared.grpc..");
        rule.check(importedClasses);
    }
}