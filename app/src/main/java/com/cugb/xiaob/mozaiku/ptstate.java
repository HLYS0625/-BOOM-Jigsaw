package com.cugb.xiaob.mozaiku;

class ptstate{
    public int currentCost=0;//当前实际代价
    public int guessCost=0;//当前实际代价估计代价
    public long state=0;
//    public int vol,col;//当前拼图块状态与目标拼图块状态
    ptstate( int _currentCost,int _guessCost,long _state){
        currentCost=_currentCost;guessCost=_guessCost;state=_state;
    }
    ptstate(ptstate _ptstate){
        currentCost = _ptstate.currentCost;
        guessCost = _ptstate.guessCost;
        state = _ptstate.state;
                }
    void setState(int _currentCost,int _guessCost, long _state){
        currentCost = _currentCost;
        guessCost = _guessCost;
        state = _state;
                }

}

class openListEle{
    ptstate pts;
    openListEle next;
    openListEle pre; //指向上一个状态，而非上一个节点
    void addNext(openListEle ole){
        if(ole == null)
        return;
        ole.next = this.next;
        this.next = ole;
        ole.pre = this;
        }
    //构造
    openListEle(){
    }
    //复制
    openListEle(openListEle copy){
        this.pts= copy.getPts();
        this.next=copy.getNext();
        this.pre=copy.getPre();
    }
    //获得pts
    private ptstate getPts(){
        return this.pts;
    }
    //获得next
   private openListEle getNext(){
        return this.next;
    }
    //获得pre
   private openListEle getPre(){
        return this.pre;
    }
}
