/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.validation;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.jetbrains.python.codeInsight.fstrings.FStringParser;
import com.jetbrains.python.codeInsight.fstrings.FStringParser.FragmentOffsets;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.jetbrains.python.psi.PyUtil.StringNodeInfo;

/**
 * @author Mikhail Golubev
 */
public class FStringsAnnotator extends PyAnnotator {
  @Override
  public void visitPyStringLiteralExpression(PyStringLiteralExpression pyString) {
    for (ASTNode node : pyString.getStringNodes()) {
      if (new StringNodeInfo(node).isFormatted()) {
        final int nodeOffset = node.getTextRange().getStartOffset();
        final List<FragmentOffsets> fragments = FStringParser.parse(node.getText());
        for (FragmentOffsets fragment : fragments) {
          if (fragment.getLeftBraceOffset() + 1 >= fragment.getContentEndOffset()) {
            report(fragment.getContentRange().shiftRight(nodeOffset), "Empty expressions are not allowed inside f-strings");
          }
        }
      }
    }
  }

  private void report(@NotNull TextRange range, @NotNull String message) {
    getHolder().createErrorAnnotation(range, message);
  }
}
