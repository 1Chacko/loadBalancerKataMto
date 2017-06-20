package edu.iis.mto.serverloadbalancer;

import static edu.iis.mto.serverloadbalancer.CurrentLoadPercentageMatcher.hasCurrentLoadPercentageOf;
import static edu.iis.mto.serverloadbalancer.ServerBuilder.server;
import static edu.iis.mto.serverloadbalancer.ServerVmsCountMatcher.hasAVmsCountOf;
import static edu.iis.mto.serverloadbalancer.VmBuilder.vm;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.hamcrest.Matcher;
import org.junit.Test;

public class ServerLoadBalancerTest {
	@Test
	public void itCompiles() {
		assertThat(true, equalTo(true));
	}

	@Test
	public void balancingServer_noVm_ServerStaysEmpty() {
		Server theServer = a(server().withCapacity(1));

		balancing(aServersListWith(theServer), anEmptyListOfVms());

		assertThat(theServer, hasCurrentLoadPercentageOf(0.0d));
	}

	@Test
	public void balancingOneServerWithOneSlotCapacity_andOneSlotVm_fillsTHeServerWithTheVm() {
		Server theServer = a(server().withCapacity(1));
		Vm theVm = a(vm().ofSize(1));

		balancing(aServersListWith(theServer), aVmsListWith(theVm));

		assertThat(theServer, hasCurrentLoadPercentageOf(100.0d));
		assertThat("server should contain the vm", theServer.contains(theVm));
	}

	@Test
	public void balancingOneServerWIthTenSlotsCapacity_andOneSlotVm_fillTheServerWithTenPercent() {
		Server theServer = a(server().withCapacity(10));
		Vm theVm = a(vm().ofSize(1));

		balancing(aServersListWith(theServer), aVmsListWith(theVm));

		assertThat(theServer, hasCurrentLoadPercentageOf(10.0d));
		assertThat("server should contain the vm", theServer.contains(theVm));
	}

	@Test
	public void balancingAServerWithEnoughRoom_getsFilledWithAllVms() {
		Server theServer = a(server().withCapacity(100));
		Vm theFirstVm = a(vm().ofSize(1));
		Vm theSecondVm = a(vm().ofSize(1));

		balancing(aServersListWith(theServer), aVmsListWith(theFirstVm, theSecondVm));

		assertThat(theServer, hasAVmsCountOf(2));
		assertThat("server should contain the first vm", theServer.contains(theFirstVm));
		assertThat("server should contain the second vm", theServer.contains(theSecondVm));
	}

	@Test
	public void aVm_shouldBeBalanced_onLessLoadedServerFirst() {
		Server lessLoadedServer = a(server().withCapacity(100).withCurentLoadOf(45.0d));
		Server moreLoadedServer = a(server().withCapacity(100).withCurentLoadOf(50.0d));

		Vm theVm = a(vm().ofSize(1));

		balancing(aServersListWith(lessLoadedServer, moreLoadedServer), aVmsListWith(theVm));

		assertThat("less loaded server should contain the vm", lessLoadedServer.contains(theVm));
		assertThat("more loaded server should not contain the vm", !moreLoadedServer.contains(theVm));
	}

	@Test
	public void balanceAServerWithNotEnoughRoom_shouldNotBeFilledWithVm() {
		Server theServer = a(server().withCapacity(10).withCurentLoadOf(90.0d));
		Vm theVm = a(vm().ofSize(2));

		balancing(aServersListWith(theServer), aVmsListWith(theVm));

		assertThat("the server should not contain the vm", !theServer.contains(theVm));
	}

	@Test
	public void balance_serversAndVms() {
		Server server1 = a(server().withCapacity(4));
		Server server2 = a(server().withCapacity(6));
		Vm vm1 = a(vm().ofSize(1));
		Vm vm2 = a(vm().ofSize(4));
		Vm vm3 = a(vm().ofSize(2));

		balancing(aServersListWith(server1, server2), aVmsListWith(vm1, vm2, vm3));
		
		assertThat(server1, hasCurrentLoadPercentageOf(75.0d));
		assertThat(server2, hasCurrentLoadPercentageOf(66.66666666666666d));
		
		assertThat("the server 1 should contain vm 1", server1.contains(vm1));
		assertThat("the server 2 should contain vm 2", server2.contains(vm2));
		assertThat("the server 1 should contain vm 3", server1.contains(vm3));
	}

	private Vm[] aVmsListWith(Vm... vms) {
		return vms;
	}

	private void balancing(Server[] servers, Vm[] vms) {
		new ServerLoadBalancer().balance(servers, vms);
	}

	private Vm[] anEmptyListOfVms() {
		return new Vm[0];
	}

	private Server[] aServersListWith(Server... servers) {
		return servers;
	}

	private <T> T a(Builder<T> builder) {
		return builder.build();
	}
}
