package processing.app.ui;

public abstract class SearchResult {
  
  private final Search s;
  
  public SearchResult(Search s) {
    this.s = s;
  }
  
  public abstract void select();
  public void close() {
    s.handleClose();
  }
}
