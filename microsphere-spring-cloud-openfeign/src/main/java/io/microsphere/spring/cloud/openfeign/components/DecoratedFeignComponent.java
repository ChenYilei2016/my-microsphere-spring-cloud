package io.microsphere.spring.cloud.openfeign.components;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.lang.NonNull;

import java.lang.reflect.Constructor;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @since 0.0.1
 */
public abstract class DecoratedFeignComponent<T> implements Refreshable {


    private final FeignContext feignContext;
    private final String contextId;

    private final FeignClientProperties clientProperties;

    protected volatile T delegate;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public DecoratedFeignComponent(String contextId, FeignContext feignContext, FeignClientProperties clientProperties, T delegate) {
        this.contextId = contextId;
        this.feignContext = feignContext;
        this.clientProperties = clientProperties;
        this.delegate = delegate;
    }

    public T delegate() {
        readLock.lock();
        if (delegate == null) {
            readLock.unlock();
            return loadInstance();
        }

        readLock.unlock();
        return this.delegate;
    }

    @NonNull
    public FeignContext getFeignContext() {
        return this.feignContext;
    }

    @NonNull
    public String contextId() {
        return this.contextId;
    }

    public void refresh() {
        writeLock.lock();
        this.delegate = null;
        writeLock.unlock();
    }

    protected abstract Class<T> componentType();

    public FeignClientProperties.FeignClientConfiguration getDefaultConfiguration() {
        return this.clientProperties.getConfig().get(this.clientProperties.getDefaultConfig());
    }

    public FeignClientProperties.FeignClientConfiguration getCurrentConfiguration() {
        return this.clientProperties.getConfig().get(contextId);
    }

    protected T loadInstance() {
        Class<T> componentType = componentType();
        String contextId = contextId();
        writeLock.lock();
        try {
            T component = getFeignContext().getInstance(contextId, componentType);
            if (component == null)
                return BeanUtils.instantiateClass(componentType);
            return component;
        } catch (Exception e) {
            return BeanUtils.instantiateClass(componentType);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate().equals(obj);
    }

    @Override
    public String toString() {
        return delegate().toString();
    }

    public static <W extends DecoratedFeignComponent<T>, T> W instantiate(Class<W> decoratedClass, Class<T> componentClass,
                                                             String contextId, FeignContext feignContext, FeignClientProperties clientProperties, T delegate) {
        try {
            Constructor<W> constructor = decoratedClass.getConstructor(String.class, FeignContext.class, FeignClientProperties.class, componentClass);
            return BeanUtils.instantiateClass(constructor, contextId, feignContext, clientProperties, delegate);
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new BeanInstantiationException(decoratedClass, noSuchMethodException.getLocalizedMessage());
        }

    }
}
