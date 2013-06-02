package tellmemore

import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import org.specs2.spring.Specification

@Transactional
@ContextConfiguration(Array("classpath*:/META-INF/it-data-sources.xml", "classpath*:/META-INF/spring-config.xml"))
abstract class IntegrationTest extends Specification
