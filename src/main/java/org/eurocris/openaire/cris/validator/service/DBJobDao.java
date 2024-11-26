package org.eurocris.openaire.cris.validator.service;

import org.eurocris.openaire.cris.validator.model.CrisJob;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class DBJobDao implements JobDao {

    @PersistenceContext(unitName = "crisEntityManager")
    private final EntityManager entityManager;

    public DBJobDao(EntityManagerFactory emf) {
        this.entityManager = emf.createEntityManager();
    }


    private TypedQuery<CrisJob> getJobsOfAdmin(String userId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CrisJob> cq = cb.createQuery(CrisJob.class);

        Root<CrisJob> job = cq.from(CrisJob.class);

        cq.select(job).where(cb.equal(job.get("admin"), userId));

        return entityManager.createQuery(cq);
    }

    @Override
    public Optional<CrisJob> get(Long id) {
        CrisJob job = entityManager.find(CrisJob.class, id);
        return Optional.ofNullable(job);
    }

    @Override
    public List<CrisJob> getJobs(String userId) {
        return getJobsOfAdmin(userId).getResultList();
    }

    @Override
    public List<CrisJob> getJobs(String userId, String validationStatus) {
        if (validationStatus.equals("all")) {
            return getJobs(userId);
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CrisJob> cq = cb.createQuery(CrisJob.class);

        Root<CrisJob> job = cq.from(CrisJob.class);

        cq.select(job)
                .where(cb.and(
                    cb.equal(job.get("admin"), userId),
                    cb.equal(job.get("status"), validationStatus)
                )
        );
        TypedQuery<CrisJob> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    @Override
    public List<CrisJob> getJobs(String userId, int offset, int size) {
        TypedQuery<CrisJob> query = getJobsOfAdmin(userId);
        query.setFirstResult(offset);
        query.setMaxResults(size);
        return query.getResultList();
    }

    @Override
    public List<CrisJob> getJobs(String userId, Date dateFrom, Date dateTo) {
        throw new UnsupportedOperationException("Method not implemented, yet");
    }

    @Override
    public List<CrisJob> getJobs(String userId, int offset, int size, Date dateFrom, Date dateTo, String validationStatus) {
        throw new UnsupportedOperationException("Method not implemented, yet");
    }

    @Override
    public List<CrisJob> getAll() {
        return entityManager
                .createQuery("from CrisJob", CrisJob.class)
                .getResultList();
    }

    @Override
    @Transactional(transactionManager = "crisTransactionManager")
    public CrisJob save(CrisJob t) {
        CrisJob managed = entityManager.merge(t);
        entityManager.flush();
        return managed;
    }

    @Override
    @Transactional(transactionManager = "crisTransactionManager")
    public void delete(CrisJob t) {
        entityManager.remove(t);
    }
}
