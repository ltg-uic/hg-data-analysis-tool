package ltg.hg.analysis.model

/**
 * Created by tebemis on 5/27/14.
 */
case class Data(ag_log: List[Entry], at_log: List[Entry], bj_log: List[Entry], stats: List[SectionStats]) {

  override def toString = "5ag length " + ag_log.length + "\n5at length " + at_log.length + "\n5bj length " + bj_log.length +
    "\nstats length " + stats.length

}