package com.chessgrinder.chessgrinder.testutil.repository;

import com.chessgrinder.chessgrinder.entities.ParticipantEntity;
import com.chessgrinder.chessgrinder.repositories.ParticipantRepository;
import org.assertj.core.util.Streams;
import org.mockito.Mockito;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.FluentQuery;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static com.chessgrinder.chessgrinder.security.entitypermissionevaluator.EntityPermissionEvaluator.getIdField;

public class TestJpaRepository<T, ID> implements JpaRepository<T, ID>, DataHolder<T, ID> {
    private final Map<ID, T> data = new HashMap<>();

    @Override
    public Map<ID, T> getData() {
        return data;
    }

    @Override
    public void flush() {
    }

    @Override
    public <S extends T> S saveAndFlush(S entity) {
        return save(entity);
    }

    @Override
    public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) {
        return Streams.stream(entities).map(this::save).toList();
    }

    @Override
    public void deleteAllInBatch(Iterable<T> entities) {
        //noinspection UseBulkOperation
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<ID> ids) {
        ids.forEach(this::deleteById);
    }

    @Override
    public void deleteAllInBatch() {
        data.clear();
    }

    @Override
    public T getOne(ID id) {
        return data.get(id);
    }

    @Override
    public T getById(ID id) {
        return data.get(id);
    }

    @Override
    public T getReferenceById(ID id) {
        return data.get(id);
    }

    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    private ID guessId(Object entity) {
        return (ID) getIdField(entity);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends T, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends T> S save(S entity) {
        data.put(guessId(entity), entity);
        return entity;
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        return Streams.stream(entities).map(this::save).toList();
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public boolean existsById(ID id) {
        return data.containsKey(id);
    }

    @Override
    public List<T> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        return Streams.stream(ids).map(this::getById).toList();
    }

    @Override
    public long count() {
        return data.size();
    }

    @Override
    public void deleteById(ID id) {
        data.remove(id);
    }

    @Override
    public void delete(T entity) {
        data.remove(guessId(entity));
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        ids.forEach(this::deleteById);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        data.clear();
    }

    @Override
    public List<T> findAll(Sort sort) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        throw new UnsupportedOperationException();
    }

    public static <R extends CrudRepository<T, ID>, T, ID> R of(Class<R> repositoryType){

        class CUR extends TestJpaRepository<ParticipantEntity, UUID> implements ParticipantRepository {
            @Override
            public List<ParticipantEntity> findByTournamentId(UUID tournamentId) {
                return getData().values().stream()
                        .filter(it -> it.getTournament() != null && it.getTournament().getId().equals(tournamentId))
                        .toList();
            }

            @Override
            public ParticipantEntity findByTournamentIdAndUserId(UUID tournamentId, UUID userId) {
                return findByTournamentId(tournamentId).stream()
                        .filter(it -> it.getUser() != null && it.getUser().getId().equals(userId))
                        .findFirst()
                        .orElse(null);
            }

            @Override
            public List<ParticipantEntity> findAllByUserId(UUID userId) {
                return List.of();
            }
        }

        TestJpaRepository<T, ID> target = new TestJpaRepository<>();
        R proxy1 = (R) Proxy.newProxyInstance(
                TestJpaRepository.class.getClassLoader(),
                new Class[]{repositoryType, CrudRepository.class, DataHolder.class},
                (proxy, method, args) -> method.invoke(target, args));

        return proxy1;
    }

    public static <R extends CrudRepository<T, ID>, T, ID> Map<ID, T> getData(R repository){
        return ((DataHolder<T, ID>) repository).getData();
    }

}