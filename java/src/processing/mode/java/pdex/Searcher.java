package processing.mode.java.pdex;

import org.eclipse.jdt.core.dom.*;
import processing.app.ui.*;
import processing.mode.java.JavaEditor;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Searcher {
  private final JavaEditor editor;
  private final PreprocessingService pps;
  
  Searcher(JavaEditor editor, PreprocessingService pps) {
    
    this.editor = editor;
    this.pps = pps;
  }
  public void search(Search s, Consumer<ArrayList<SearchResult>> callback) {
    pps.whenDoneBlocking(ps -> handleSearch(ps, s, callback));
  }
  
  private void handleSearch(PreprocessedSketch ps, Search search, Consumer<ArrayList<SearchResult>> callback) {
    ArrayList<SearchResult> occurrences = new ArrayList<>();
    ps.compilationUnit.getRoot().accept(new ASTVisitor() {
      @Override
      public boolean visit(TypeDeclaration name) {
        if (search.match(name.getName().getIdentifier())) occurrences.add(new SNSearchResult(search, name, ps));
        return super.visit(name);
      }
    });
    callback.accept(occurrences);
  }
  
  class SNSearchResult extends SearchResult {
    
    private final TypeDeclaration item;
    private final PreprocessedSketch ps;
  
    SNSearchResult(Search s, TypeDeclaration item, PreprocessedSketch ps) {
      super(s);
      this.item = item;
      this.ps = ps;
    }
    
    public void select() {
      PreprocessedSketch.SketchInterval si = ps.mapJavaToSketch(item.getName());
      if (!ps.inRange(si)) return;
      EventQueue.invokeLater(() -> editor.highlight(si.tabIndex, si.startTabOffset, si.stopTabOffset));
      close();
    }
    
    @Override public String toString() {
      return item.getName().getIdentifier();
    }
  }
}
