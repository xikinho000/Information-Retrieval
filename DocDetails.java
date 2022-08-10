/**
 * Developer Kamel Assaf
 * Date modified : 27-May-2018
 * Date updated  : 27-Jul-2022
 */
public class DocDetails{
        //Integer docId;
	Integer docLen;
	Integer maxTf;
	Integer termFrequency;
	
	public DocDetails(Integer docLen, Integer maxTf2, Integer termFrequency) {
	//	this.docId = docId;
                this.docLen = docLen;
		this.maxTf = maxTf2;
		this.termFrequency = termFrequency;
	}
       // public Integer getDocId(){
       //     return docId;
      //  }
      //  public void setDocId(Integer docId){
      ////      this.docId = docId;
      //  }
	public Integer getTermFrequency() {
		return termFrequency;
	}

	public void setTermFrequency(Integer termFrequency) {
		this.termFrequency = termFrequency;
	}

	public Integer getDocLen() {
		return docLen;
	}

	public void setDocLen(Integer docLen) {
		this.docLen = docLen;
	}

	public Integer getMaxTf() {
		return maxTf;
	}

	public void setMaxTf(Integer maxTf) {
		this.maxTf = maxTf;
	}
}
