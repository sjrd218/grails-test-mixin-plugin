package grails.test.mixin

import junit.framework.JUnit4TestAdapter
import junit.framework.TestResult
import org.junit.After
import org.junit.Before
import org.junit.runner.Result
import org.junit.runner.notification.RunNotifier
import org.spockframework.runtime.Sputnik
import spock.lang.Specification

/**
 * Tests for the mixin that adds functionality to a test case
 *
 * @author Graeme Rocher
 *
 */
class TestMixinSpec extends Specification {



    void "Test that appropriate test hooks are called for a JUnit 3 test"() {
        setup:
           MyMixin.doFirstCalled = false
           MyMixin.doLastCalled = false
        when:
            def test = junit3Test
            test.setUp()
            test.testSomething()
            test.tearDown()

        then:
            MyMixin.doFirstCalled == true
            MyMixin.doLastCalled == true
    }

    void "Test that appropriate test hooks are called for a JUnit 4 test"() {
        setup:
           MyMixin.doFirstCalled = false
           MyMixin.doLastCalled = false
        when:
            def test = junit4Test
            def adapter = new JUnit4TestAdapter(test.getClass())
            final result = new TestResult()
            adapter.run(result)

        then:
            result.runCount() == 1
            result.failureCount() == 0
            MyMixin.doFirstCalled == true
            MyMixin.doLastCalled == true
    }

    void "Test that appropriate test hooks are called for a Spock test"() {
        setup:
           MyMixin.doFirstCalled = false
           MyMixin.doLastCalled = false
        when:
            def test = spockTest
            def adapter = new Sputnik(test.getClass())
            final notifier = new RunNotifier()
            def result = new Result()
            notifier.addListener(result.createListener())
            adapter.run(notifier)


        then:
            result.runCount == 1
            result.failureCount == 0

            // TODO: These won't work until we upgrade Spock. Next version of Spock will support @Before/@After
            MyMixin.doFirstCalled == false
            MyMixin.doLastCalled == false
    }

    def getJunit3Test() {
        new GroovyClassLoader().parseClass('''
@grails.test.mixin.TestMixin(grails.test.mixin.MyMixin)
class MyJunit3Test extends GroovyTestCase {

    void testSomething() {
        callMe()
    }
}
''').newInstance()
    }


    def getJunit4Test() {
        new GroovyClassLoader().parseClass('''
@grails.test.mixin.TestMixin(grails.test.mixin.MyMixin)

class MyJunit4Test {

    @org.junit.Test
    void testSomething() {
        callMe()
    }
}
''').newInstance()
    }

    def getSpockTest() {
        new GroovyClassLoader().parseClass('''
@grails.test.mixin.TestMixin(grails.test.mixin.MyMixin)
class MyJunitSpockTest extends spock.lang.Specification {

    void "Test something"() {
        when:
            callMe()
        then:
            true == true
    }
}
''').newInstance()
    }
}


class MyMixin {
    static doFirstCalled = false
    static doLastCalled = false
    @Before
    void doFirst() {
         doFirstCalled = true
    }

    void callMe() {
        // do nothing
    }

    @After
    void doLast() {
        doLastCalled = true
    }
}
