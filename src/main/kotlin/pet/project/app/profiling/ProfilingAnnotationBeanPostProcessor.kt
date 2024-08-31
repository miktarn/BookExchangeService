package pet.project.app.profiling

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import pet.project.app.annotation.Profiling
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

@Component
class ProfilingAnnotationBeanPostProcessor(private val profilingConsumer: ProfilingConsumer) : BeanPostProcessor {

    private val beanClassesForProfilingMap = hashMapOf<String, KClass<out Any>>()
    private val profilingInterceptor = LoggerProfilingMethodInterceptor(profilingConsumer)

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        val beanClass: KClass<out Any> = bean::class
        if (beanClass.annotations.contains(Profiling())) {
            beanClassesForProfilingMap[beanName] = beanClass
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        return if (beanClassesForProfilingMap.containsKey(beanName)) {
            val factory = ProxyFactory(bean)
            factory.addAdvice(profilingInterceptor)
            factory.proxy
        } else {
            bean
        }
    }

    class LoggerProfilingMethodInterceptor(private val profilingConsumer: ProfilingConsumer) : MethodInterceptor {

        override fun invoke(invocation: MethodInvocation): Any? {
            val before = System.nanoTime()
            try {
                return invocation.proceed()
            } finally {
                val after = System.nanoTime()
                val executionDuration: Duration = after.nanoseconds - before.nanoseconds
                profilingConsumer.accept(ProfilingData(invocation.method, executionDuration))
            }
        }
    }

}