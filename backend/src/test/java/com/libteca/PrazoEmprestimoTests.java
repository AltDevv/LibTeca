package com.libteca;
import com.libteca.enums.PrazoEmprestimo;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PrazoEmprestimoTests {
    @Test void calendarioRespeitaFimDoMesEAnoBissexto(){
        assertEquals(LocalDate.of(2027,2,28),PrazoEmprestimo.UM_MES.calcular(LocalDate.of(2027,1,31)));
        assertEquals(LocalDate.of(2028,2,29),PrazoEmprestimo.UM_MES.calcular(LocalDate.of(2028,1,31)));
        assertEquals(LocalDate.of(2027,2,28),PrazoEmprestimo.DOIS_MESES.calcular(LocalDate.of(2026,12,31)));
        assertEquals(LocalDate.of(2027,1,7),PrazoEmprestimo.UMA_SEMANA.calcular(LocalDate.of(2026,12,31)));
    }
}
