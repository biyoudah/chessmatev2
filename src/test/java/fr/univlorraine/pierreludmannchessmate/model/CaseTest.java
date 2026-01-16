package fr.univlorraine.pierreludmannchessmate.model;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CaseTest {

    @Test
    public void CaseVideTest() {
        Case c = new Case(1,1,true,null);
        assertEquals(1,c.getX());
        assertEquals(1,c.getY());
        assertTrue(c.isEstVide());
        assertNull(c.getPiece());
    }

    @Test
    public void CaseDameTest() {
        Dame d = new Dame(true);
        Case c = new Case(1,1,false,d);
        assertEquals(1,c.getX());
        assertEquals(1,c.getY());
        assertFalse(c.isEstVide());
        assertEquals(d,c.getPiece());
    }

    @Test
    public void isEstVideTest() {
        Case c = new Case(1,1,true,null);
        assertTrue(c.isEstVide());
    }

    @Test
    public void isEstVideTest1() {
        Dame d = new Dame(true);
        Case c = new Case(1,1,false,d);
        assertFalse(c.isEstVide());
    }

    @Test
    public void setEstVideTest() {
        Case c = new Case(1,1,false,null);
        c.setEstVide(true);
        assertTrue(c.isEstVide());
    }

    @Test
    public void getPieceTest() {
        Case c = new Case(1,1,false,null);
        assertNull(c.getPiece());
    }

    @Test
    public void setPieceTest() {
        Dame d = new Dame(true);
        Case c = new Case(1,1,false,null);
        c.setPiece(d);
        assertEquals(d,c.getPiece());
    }

    @Test
    public void getXTest() {
        Case c = new Case(1,1,false,null);
        assertEquals(1,c.getX());
    }

    @Test
    public void getYTest() {
        Case c = new Case(1,1,false,null);
        assertEquals(1,c.getY());
    }
}
