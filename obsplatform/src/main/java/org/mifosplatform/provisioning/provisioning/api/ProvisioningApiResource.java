package org.mifosplatform.provisioning.provisioning.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.finance.payments.data.McodeData;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.ippool.data.IpPoolData;
import org.mifosplatform.organisation.ippool.service.IpPoolManagementReadPlatformService;
import org.mifosplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.portfolio.client.service.GroupData;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.mifosplatform.portfolio.order.data.OrderLineData;
import org.mifosplatform.portfolio.order.service.OrderReadPlatformService;
import org.mifosplatform.portfolio.servicemapping.service.ServiceMappingReadPlatformService;
import org.mifosplatform.provisioning.provisioning.data.ProcessRequestData;
import org.mifosplatform.provisioning.provisioning.data.ProvisioningCommandParameterData;
import org.mifosplatform.provisioning.provisioning.data.ProvisioningData;
import org.mifosplatform.provisioning.provisioning.data.ServiceParameterData;
import org.mifosplatform.provisioning.provisioning.service.ProvisioningReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author ashokreddy
 *
 */
@Path("/provisioning")
@Component
@Scope("singleton")
public class ProvisioningApiResource {
	
	private  final Set<String> RESPONSE_DATA_PARAMETERS=new HashSet<String>(Arrays.asList("id","cancelledStatus","status","contractPeriod","nextBillDate","flag",
	           "currentDate","plan_code","units","service_code","allowedtypes","data","servicedata","billing_frequency", "start_date", "contract_period",
	           "billingCycle","startDate","invoiceTillDate","orderHistory","userAction","ispaymentEnable","paymodes","serviceDatas","groupDatas"));
	
	  private final String resourceNameForPermissions = "PROVISIONINGSYSTEM";
	  
	  private final PlatformSecurityContext context;
	  private final DefaultToApiJsonSerializer<ProvisioningData> toApiJsonSerializer;
	  private final ApiRequestParameterHelper apiRequestParameterHelper;
	  private final ProvisioningReadPlatformService provisioningReadPlatformService;
	  private final MCodeReadPlatformService codeReadPlatformService;
	  private final OrderReadPlatformService orderReadPlatformService;
	  private final ServiceMappingReadPlatformService serviceMappingReadPlatformService;
	  private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	  private final IpPoolManagementReadPlatformService ipPoolManagementReadPlatformService;
	  private final DefaultToApiJsonSerializer<ProcessRequestData> toApiJsonSerializerProcessRequest;
	  private final GroupReadPlatformService groupReadPlatformService;
	  
	 
	  
	   @Autowired
	   public ProvisioningApiResource(final PlatformSecurityContext context,final ConfigurationRepository configurationRepository,  
	   final ApiRequestParameterHelper apiRequestParameterHelper,final DefaultToApiJsonSerializer<ProvisioningData> toApiJsonSerializer,
	   final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,final ProvisioningReadPlatformService provisioningReadPlatformService,
	   final MCodeReadPlatformService codeReadPlatformService,final OrderReadPlatformService orderReadPlatformService,final ServiceMappingReadPlatformService mappingReadPlatformService,
	   final IpPoolManagementReadPlatformService ipPoolManagementReadPlatformService,final DefaultToApiJsonSerializer<ProcessRequestData> toApiJsonSerializerProcessRequest,
	   final GroupReadPlatformService groupReadPlatformService) {
	 
		  
		        this.context = context;
		        this.apiRequestParameterHelper = apiRequestParameterHelper;
		        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		        this.toApiJsonSerializer=toApiJsonSerializer;
		        this.provisioningReadPlatformService=provisioningReadPlatformService;
		        this.codeReadPlatformService=codeReadPlatformService;
		        this.orderReadPlatformService=orderReadPlatformService;
		        this.ipPoolManagementReadPlatformService=ipPoolManagementReadPlatformService;
		        this.toApiJsonSerializerProcessRequest=toApiJsonSerializerProcessRequest;
		        this.serviceMappingReadPlatformService=mappingReadPlatformService;
		        this.groupReadPlatformService=groupReadPlatformService;
		       
		    }
	
	 @GET
	 @Consumes({MediaType.APPLICATION_JSON})
	 @Produces({MediaType.APPLICATION_JSON})
		public String retrieveProvisiongSystemDetail(@Context final UriInfo uriInfo) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<ProvisioningData> datas=this.provisioningReadPlatformService.getProvisioningData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
	    return this.toApiJsonSerializer.serialize(settings, datas, RESPONSE_DATA_PARAMETERS);
		}
	 
	 @GET
	 @Path("template")
	 @Consumes({MediaType.APPLICATION_JSON})
	 @Produces({MediaType.APPLICATION_JSON})
		public String retrieveTemplate(@Context final UriInfo uriInfo) {
		
		 context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		 final List<McodeData> provisioning=this.provisioningReadPlatformService.retrieveProvisioningCategory();
		 final List<McodeData> commands=this.provisioningReadPlatformService.retrievecommands();
		 final ProvisioningData data=new ProvisioningData(provisioning,commands);
		 final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
	     return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_DATA_PARAMETERS);
		}
	 
	 @GET
	 @Path("{provisioningId}")
	 @Consumes({MediaType.APPLICATION_JSON})
	 @Produces({MediaType.APPLICATION_JSON})
		public String retrieveSingleData(@Context final UriInfo uriInfo,@PathParam("provisioningId") final Long id) {
		 
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);	
		final ProvisioningData data= this.provisioningReadPlatformService.retrieveIdData(id);
		final List<McodeData> provisioning=this.provisioningReadPlatformService.retrieveProvisioningCategory();
		final List<McodeData> commands=this.provisioningReadPlatformService.retrievecommands();
		final List<ProvisioningCommandParameterData> commandParameters=this.provisioningReadPlatformService.retrieveCommandParams(id);
		data.setCommands(commands);
		data.setProvisioning(provisioning);
		data.setCommandParameters(commandParameters);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
	    return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_DATA_PARAMETERS);
		}
	 
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String addProvisiongSystemDetails(final String apiRequestBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().provisiongSystem().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@PUT
	@Path("{provisioningId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateProvisiongSystemDetail(
			@PathParam("provisioningId") final Long id,
			final String apiRequestBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateprovisiongSystem(id).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@DELETE
	@Path("{provisioningId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteProvisioningSystem(
			@PathParam("provisioningId") final Long id,
			final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteProvisiongSystem(id).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@POST
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String newActiveProvisioningDetails(
			@PathParam("clientId") final Long clientId,
			final String apiRequestBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().addNewProvisioning(clientId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@GET
	@Path("provisiontemplate/{orderId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveProvisionTemplateData(
			@PathParam("orderId") final Long orderId,
			@QueryParam("serviceId") Long serviceId,
			@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<ServiceParameterData> parameterDatas = this.serviceMappingReadPlatformService.getSerivceParameters(orderId, serviceId);
		final List<ServiceParameterData> serviceDatas = this.provisioningReadPlatformService.getSerivceParameters(orderId);
		final Collection<MCodeData> vlanDatas = this.provisioningReadPlatformService.retrieveVlanDetails("VLANS");
		final List<IpPoolData> ipPoolDatas = this.ipPoolManagementReadPlatformService.getUnallocatedIpAddressDetailds();
		final List<OrderLineData> services = this.orderReadPlatformService.retrieveOrderServiceDetails(orderId);
		final Collection<GroupData> groupDatas = this.groupReadPlatformService.retrieveGroupServiceDetails(orderId);
		final ProvisioningData provisioningData = new ProvisioningData(vlanDatas, ipPoolDatas, services, serviceDatas, parameterDatas, groupDatas);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, provisioningData, RESPONSE_DATA_PARAMETERS);
	}

	@GET
	@Path("serviceparmas/{orderId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveOrderServicesData(
			@PathParam("orderId") final Long orderId,
			@QueryParam("serviceId") final Long serviceId,
			@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<ServiceParameterData> parameterDatas = this.serviceMappingReadPlatformService.getSerivceParameters(orderId, serviceId);
		final List<ServiceParameterData> serviceDatas = this.provisioningReadPlatformService.getProvisionedSerivceParameters(orderId);
		final Collection<MCodeData> vlanDatas = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_VLANS);
		final List<IpPoolData> ipPoolDatas = this.ipPoolManagementReadPlatformService.getUnallocatedIpAddressDetailds();
		final List<OrderLineData> services = this.orderReadPlatformService.retrieveOrderServiceDetails(orderId);
		final Collection<GroupData> groupDatas = this.groupReadPlatformService.retrieveGroupServiceDetails(orderId);
		final ProvisioningData provisioningData = new ProvisioningData(vlanDatas, ipPoolDatas, services, serviceDatas, parameterDatas, groupDatas);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, provisioningData,RESPONSE_DATA_PARAMETERS);
	}

	@PUT
	@Path("serviceparams/{orderId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateProvisiongServiceParams(@PathParam("orderId") final Long orderId, final String apiRequestBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateprovisiongServiceParams(orderId)
				.withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@PUT
	@Path("confirm/{processrequestId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String confirmProvisiongDetails(@PathParam("processrequestId") final Long processrequestId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().confirnProvisiongDetails(processrequestId).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@PUT
	@Path("updateprovisiondetails/{processrequestId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateProvisiongDetails(@PathParam("processrequestId") final Long processrequestId) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateprovisiongDetails(processrequestId).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@GET
	@Path("template/{orderNo}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveProcessRequest(@Context final UriInfo uriInfo, @PathParam("orderNo") final String orderNo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<ProcessRequestData> provisioning = this.provisioningReadPlatformService.getProcessRequestData(orderNo);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializerProcessRequest.serialize(settings, provisioning, RESPONSE_DATA_PARAMETERS);
	}

	@GET
	@Path("processRequest/{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveProcessRequestId(@Context final UriInfo uriInfo, @PathParam("id") final Long id) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final ProcessRequestData provisioning = this.provisioningReadPlatformService.getProcessRequestIDData(id);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializerProcessRequest.serialize(settings, provisioning, RESPONSE_DATA_PARAMETERS);

	}

	@PUT
	@Path("ipdetails/{orderId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateIpDetails(@PathParam("orderId") final Long orderId,final String requestData) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder()
				.updateIpDetails(orderId).withJson(requestData).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService
				.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
		
	@GET
	@Path("client/{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveProcessClientRequest(@Context final UriInfo uriInfo, @PathParam("clientId") final Long clientId) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		//final ProcessRequestData provisioning = this.provisioningReadPlatformService.getProcessRequestClientData(clientId);
		final List<ProcessRequestData> provisioning = this.provisioningReadPlatformService.getProcessRequestClientData(clientId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializerProcessRequest.serialize(settings, provisioning, RESPONSE_DATA_PARAMETERS);
	}

	
	
	
	
	

}
