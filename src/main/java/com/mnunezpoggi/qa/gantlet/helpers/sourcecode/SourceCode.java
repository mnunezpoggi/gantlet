/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mnunezpoggi.qa.gantlet.helpers.sourcecode;

import com.inamik.text.tables.GridTable;
import com.inamik.text.tables.SimpleTable;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;

/**
 *
 * 
 */
public class SourceCode {

    public String ClassName;
    public int line;
    public String code;


    @Override
    public String toString() {
                SimpleTable s = SimpleTable.of()
                .nextRow()
                .nextCell().addLine(ClassName + " @line " + line)
                           .addLine("").addLines(code.split("\n"));
        GridTable g = Border.of(Border.Chars.of('+', '-', '|')).apply(s.toGrid());
        return Util.asString(g);
    }
}
