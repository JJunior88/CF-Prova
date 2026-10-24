package br.com.cfprova;
import org.junit.Test;
import static org.junit.Assert.*;
public class GradingTest {
    @Test public void singleFilledBubbleIsRead(){assertEquals(2,Grading.classify(new double[]{0,.02,.9,.03,0}));}
    @Test public void emptyRowIsBlank(){assertEquals(-1,Grading.classify(new double[]{.02,.03,.01,0,.02}));}
    @Test public void doubleMarkNeedsReview(){assertEquals(-2,Grading.classify(new double[]{.8,0,.9,0,0}));}
    @Test public void faintMarkNeedsReview(){assertEquals(-2,Grading.classify(new double[]{0,.3,0,0,0}));}
    @Test public void secondPartialMarkNeedsReview(){assertEquals(-2,Grading.classify(new double[]{.8,.3,0,0,0}));}
    @Test public void gradeUsesEqualWeightsAndCountsBlanksAsWrong(){int[] key={0,1,2,3,4};int[] answers={0,1,-1,0,4};assertEquals(3,Grading.correct(key,answers));assertEquals(6,Grading.grade(key,answers),.001);}
    @Test public void allCorrectIsTen(){assertEquals(10,Grading.grade(new int[]{0,4},new int[]{0,4}),.001);}
    @Test public void allWrongIsZero(){assertEquals(0,Grading.grade(new int[]{0,4},new int[]{1,-1}),.001);}
    @Test(expected=IllegalArgumentException.class) public void mismatchedLengthsAreRejected(){Grading.grade(new int[]{0},new int[]{0,1});}
    @Test(expected=IllegalArgumentException.class) public void emptyExamIsRejected(){Grading.grade(new int[]{},new int[]{});}
}
