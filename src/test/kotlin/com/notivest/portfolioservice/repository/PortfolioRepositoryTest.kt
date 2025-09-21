package com.notivest.portfolioservice.repository

import com.notivest.portfolioservice.models.portfolio.PortfolioEntity
import com.notivest.portfolioservice.models.portfolio.PortfolioStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE) // usa H2 + Flyway del profile test
class PortfolioRepositoryTest {
    @Autowired
    lateinit var portfolioRepository: PortfolioRepository

    private fun newPortfolio(
        userId: UUID,
        deleted: Boolean = false,
    ): PortfolioEntity {
        val p =
            PortfolioEntity(
                userId = userId,
                name = "Main",
                baseCurrency = "USD",
                status = PortfolioStatus.ACTIVE,
            )
        if (deleted) p.deletedAt = Instant.now()
        return portfolioRepository.saveAndFlush(p)
    }

    @Test
    fun `findAllByUserIdAndDeletedAtIsNull filtra soft-delete y pagina`() {
        val user = UUID.randomUUID()
        val keep = newPortfolio(user, deleted = false)
        newPortfolio(user, deleted = true) // filtrado
        newPortfolio(UUID.randomUUID(), deleted = false) // otro usuario

        val page =
            portfolioRepository.findAllByUserIdAndDeletedAtIsNull(
                user,
                PageRequest.of(0, 10),
            )

        assertThat(page.totalElements).isEqualTo(1)
        assertThat(page.content).extracting("id").containsExactly(keep.id)
    }

    @Test
    fun `findByIdAndUserIdAndDeletedAtIsNull respeta user y soft-delete`() {
        val user = UUID.randomUUID()
        val kept = newPortfolio(user, deleted = false)
        val deleted = newPortfolio(user, deleted = true)

        val ok = portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(kept.id!!, user)
        val notFoundByDelete = portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(deleted.id!!, user)
        val notFoundByUser = portfolioRepository.findByIdAndUserIdAndDeletedAtIsNull(kept.id!!, UUID.randomUUID())

        assertThat(ok).isPresent
        assertThat(notFoundByDelete).isNotPresent
        assertThat(notFoundByUser).isNotPresent
    }
}
