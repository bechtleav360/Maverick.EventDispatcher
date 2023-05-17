package org.av360.maverick.eventdispatcher.subscriptions;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.*;

class EventDispatcherSubscriptionsApplicationTest {
/*
Rules:
- no one can access API
- API can only access Services
- Service can only access Storage
- Storage can access nothing
- RPC layer only accessed through Services

Data:
- DTO only in API Package (no access from outside)
- Entity only in Storage Package
- Message only in RPC Package (in shared)

Shared:
- Model access by everyone
 */
    @Test
    void apiCanOnlyAccessServicesAndNoOneCanAccessApi() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("org.av360.maverick.eventdispatcher.subscriptions");

        ArchRule rule1 = noClasses()
                .that().resideInAPackage("..api..")
                .should().accessClassesThat()
                .resideOutsideOfPackages("..api..", "..services..", "org.av360.maverick.eventdispatcher.shared..");

        ArchRule rule2 = noClasses()
                .that().resideOutsideOfPackages("..api..")
                .should().accessClassesThat()
                .resideInAPackage("..api..");

        rule1.check(importedClasses);
        rule2.check(importedClasses);
    }

    @Test
    void servicesCanOnlyAccessRepo() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("org.av360.maverick.eventdispatcher.subscriptions");

        ArchRule rule = noClasses()
                .that().resideInAPackage("..services..")
                .should().accessClassesThat()
                .resideOutsideOfPackages("..repo..", "..services..");

        rule.check(importedClasses);
    }

    @Test
    void repoCannotAccessAnything() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("org.av360.maverick.eventdispatcher.subscriptions");

        ArchRule rule = noClasses()
                .that().resideInAPackage("..repo..")
                .should().accessClassesThat()
                .resideOutsideOfPackages("..repo..");

        rule.check(importedClasses);
    }

    @Test
    public void rpcLayerOnlyAccessedThroughServices() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("org.av360.maverick.eventdispatcher.subscriptions");

        ArchRule rule = noClasses()
                .that().resideOutsideOfPackages("..grpc..", "..services..")
                .should().accessClassesThat()
                .resideInAPackage("..grpc..");

        rule.check(importedClasses);
    }
}