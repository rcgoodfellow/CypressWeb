package net.deterlab.cypress.io.fedd

import net.deterlab.cypress.model.Experiment
import scalaxb.DataRecord

object TopDLGen {
  import generated._

  def apply(implicit exp: Experiment) : TopologyType = {
      TopologyType(
        version = "0",
        substrates = genSubstrates,
        elements = genElements,
        attribute = genAttributes)
  }

  def genSubstrates(implicit exp: Experiment) : Seq[SubstrateType] = {
    exp.substrates.map(x => {
      SubstrateType(
        name = x.name,
        capacity = Some(CapacityType(rate=1000, kind = Max)),
        latency = Some(LatencyType(time = 5, kind = Average)),
        attribute = Seq(),
        localname = Seq(x.name),
        status = None,
        service = Seq(),
        operation = Seq()
      )
    })
  }

  def genElements(implicit exp: Experiment) : Seq[ElementType] = {
    exp.computers.map(c => {
      val ct = ComputerType(
        name = c.name,
        cpu = Seq(),
        os = c.os.map(x => OperatingsystemType(Some(x))),
        software = c.software.map(x => SoftwareType(x.name)),
        storage = Seq(),
        interface = c.interfaces.map(x =>
          InterfaceType(
            substrate = x.substrates.toSeq,
            name = x.name,
            capacity = Some(CapacityType(rate=1000, kind = Max)),
            latency = Some(LatencyType(time=5, kind = Average)),
            attribute = Seq()
          )
        ),
        attribute = Seq(AttributeType("testbed", "deter")),
        localname = Seq(c.name),
        status = None,
        service = Seq(),
        operation = Seq()
      )
      ElementType(DataRecord(None, Some("computer"), ct))
    })
  }

  def genAttributes(implicit exp: Experiment) : Seq[AttributeType] = {
    Seq[AttributeType]()
  }


}