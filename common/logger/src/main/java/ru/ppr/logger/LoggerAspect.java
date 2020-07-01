package ru.ppr.logger;

import android.support.annotation.NonNull;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

/**
 * Created by Dmitry Nevolin on 01.02.2016.
 */
@Aspect
public class LoggerAspect {
    //разделители
    private static final String D_A = ", ";
    private static final String D_R = " -> ";
    //символы
    private static final String S_D = ".";
    private static final String S_E = "";
    private static final String S_N = "\n";
    private static final String S_PL = "(";
    private static final String S_PR = ")";
    private static final String S_T = "    ";
    //ключевые слова
    private static final String W_E = "******end******";
    private static final String W_N = "null";
    private static final String W_P = "ru.ppr.cppk";
    private static final String W_S = "*****start*****";
    private static final String W_V = "void";
    //нужный нам тип JoinPoint
    private static final String JP_KIND = "method-execution";

    @Retention(RetentionPolicy.RUNTIME)
    public @interface IncludeClass {
    }

//    @Retention(RetentionPolicy.RUNTIME)
//    public @interface IncludeMethod {
//    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface ExcludeMethod {
    }

    @Pointcut("adviceexecution() && within(LoggerAspect)")
    public void advice() {
    }

    //    @Pointcut("(execution(* @ru.ppr.cppk.helpers.LoggerAspect.IncludeClass ru.ppr.cppk..*(..)) || " +
//            "@annotation(ru.ppr.cppk.helpers.LoggerAspect.IncludeMethod)) &&" +
//            "!@annotation(ru.ppr.cppk.helpers.LoggerAspect.ExcludeMethod)"
//    )
//    @Pointcut("execution(public * @ru.ppr.cppk.helpers.LoggerAspect.IncludeClass ru.ppr.cppk..*(..)) && !@annotation(ru.ppr.cppk.helpers.LoggerAspect.ExcludeMethod)")
    @Pointcut("execution(public * @ru.ppr.logger.LoggerAspect.IncludeClass ru.ppr.cppk..*(..)) && !@annotation(ru.ppr.logger.LoggerAspect.ExcludeMethod)")
    public void method() {
    }

    @Before("method() && !cflow(advice())")
    public void before(JoinPoint joinPoint) {
        //берем сигнатуру обёрнутого кода
        final Signature signature = joinPoint.getSignature();
        //проверяем, что обёрнутый код - это метод + что текущий объект - холдер метода
        if (signature instanceof MethodSignature && JP_KIND.equals(joinPoint.getKind())) {
            final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

            String classSimpleName = method.getDeclaringClass().getName();
            String prefix = "";

            if (classSimpleName.toUpperCase().contains("PRINTER")) {
                prefix = "Printer|";
            }

            Logger.info(method.getDeclaringClass(), prefix + method.getName() + "() START ASPECT");
        }
    }

    @AfterReturning(pointcut = "method() && !cflow(advice())", returning = "result")
    public void after(@NonNull final JoinPoint joinPoint, Object result) {
        //берем сигнатуру обёрнутого кода
        final Signature signature = joinPoint.getSignature();
        //проверяем, что обёрнутый код - это метод + что текущий объект - холдер метода
        if (signature instanceof MethodSignature && JP_KIND.equals(joinPoint.getKind())) {
            final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            final Method method = methodSignature.getMethod();

            //пусть дефолтное значение будет void, чтобы было наглядней видно что это выход
            //из метода, в случае если он возвращает void
            String string = W_V;
            //значит метод возвращает не void
            if (!W_V.equals(methodSignature.getReturnType().getName()))
                string = result == null ? W_N : result.toString();

            String classSimpleName = method.getDeclaringClass().getName();
            String prefix = "";

            if (classSimpleName.toUpperCase().contains("PRINTER")) {
                prefix = "Printer|";
            }

            Logger.info(method.getDeclaringClass(), prefix + method.getName() + "() " + string + " FINISH ASPECT");
        }
    }

//    @Before("method() && !cflow(advice())")
//    public void before(JoinPoint joinPoint) {
//        //берем сигнатуру обёрнутого кода
//        final Signature signature = joinPoint.getSignature();
//        //проверяем, что обёрнутый код - это метод + что текущий объект - холдер метода
//        if (signature instanceof MethodSignature && JP_KIND.equals(joinPoint.getKind()))
//            logStartBlock(joinPoint);
//    }
//
//    @AfterReturning(pointcut = "method() && !cflow(advice())", returning = "result")
//    public void after(@NonNull final JoinPoint joinPoint, Object result) {
//        //берем сигнатуру обёрнутого кода
//        final Signature signature = joinPoint.getSignature();
//        //проверяем, что обёрнутый код - это метод + что текущий объект - холдер метода
//        if (signature instanceof MethodSignature && JP_KIND.equals(joinPoint.getKind()))
//            logEndBlock(joinPoint, result);
//    }
//
//    //пишет в лог блок начала метода
//    private static void logStartBlock(@NonNull final JoinPoint joinPoint) {
//        Logger.trace(W_S +                    //ключевое слово, указывающее, что это блок начала метода
//                S_N +                         //символ новой строки
//                S_N +                         //символ новой строки
//                methodToStringBase(joinPoint) //базовое представление метода
//        );
//    }
//
//    //пишет в лог блок окончания метода
//    private static void logEndBlock(@NonNull final JoinPoint joinPoint, final Object result) {
//        String resultString = resultToString(joinPoint, result);
//        //форматируем строку входных параметров, если не пустая
//        if (!resultString.isEmpty())
//            resultString = D_R +  //разделитель метода и результата его выполнения
//                    resultString; //результ выполнения метода
//
//        Logger.trace(W_E +                        //ключевое слово, указывающее, что это блок окончания метода
//                S_N +                             //символ новой строки
//                S_N +                             //символ новой строки
//                methodToStringBase(joinPoint) +   //базовое представление метода
//                resultString                      //форматированный результ выполнения метода
//        );
//    }
//
//    //формирует базовое представление метода
//    private static String methodToStringBase(@NonNull final JoinPoint joinPoint) {
//        final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
//        final Object called = joinPoint.getThis();
//        final Object[] args = joinPoint.getArgs();
//
//        String argsString = argsToString(args);
//        //форматируем строку аргументов, если не пустая
//        if (!argsString.isEmpty())
//            argsString = S_N +   //символ новой строки
//                    S_T +        //табуляция
//                    argsString + //входные параметры
//                    S_N;         //символ новой строки
//
//
//        return methodNameToString(method, called) + //имя метода
//                S_PL +                              //открывающая скобка
//                argsString +                        //форматированные входные параметры
//                S_PR;                               //закрывающая скобка
//    }
//
//    //конвертирует результат выполнения метода в строку
//    @NonNull
//    private static String resultToString(@NonNull final JoinPoint joinPoint, final Object result) {
//        final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
//
//        String string = S_E;
//        //значит метод возвращает не void
//        if (!W_V.equals(methodSignature.getReturnType().getName()))
//            string = objectToString(result);
//
//        return string;
//    }
//
//    //конвертирует список аргументов в строку
//    @NonNull
//    private static String argsToString(final Object[] args) {
//        String string = S_E;
//        //значит у метода есть аргументы
//        if (args != null && args.length > 0) {
//            for (Object arg : args)
//                string += objectToString(arg) + D_A; //добавляем запятую с пробелом, в качестве разделителя
//            //убираем лишний разделитель в конце
//            string = string.substring(0, string.length() - D_A.length());
//        }
//
//        return string;
//    }
//
//    //конвертирует полное имя метода в строку
//    @NonNull
//    private static String methodNameToString(@NonNull final Method method, final Object called) {
//        //префикс: состоит из пакета, класса и ссылки на объект, у которого вызывается метод,
//        //если called == null, значит метод статический и ссылки на объект нет.
//        //имя метода: префикс . сокращённое имя
//        return objectToString(called == null ? method.getDeclaringClass().getCanonicalName()
//                : called.toString()) + S_D + method.getName();
//    }
//
//    //конвертирует объект в строку
//    @NonNull
//    private static String objectToString(final Object object) {
//        //объект может быть null
//        String string = object == null ? W_N : object.toString();
//        //убираем пакет внутри которого логгируем вызовы, для уменьшения количества
//        //текста ибо и так понятно что там за пакет, имя класса с таким пакетом
//        //будет относительным и начинаться с точки
//        if (string.startsWith(W_P))
//            string = string.replace(W_P, S_E);
//
//        return string;
//    }

}
