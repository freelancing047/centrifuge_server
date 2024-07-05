
package gov.ic.dodiis.service.security.dias.v2_1.authservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the gov.ic.dodiis.service.security.dias.v2_1.authservice package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetAllGroupsResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getAllGroupsResponse");
    private final static QName _GetGroupMembersRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getGroupMembersRequest");
    private final static QName _GetUserClearanceRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getUserClearanceRequest");
    private final static QName _GetAllUserGroupsRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getAllUserGroupsRequest");
    private final static QName _IsMemberOfRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "isMemberOfRequest");
    private final static QName _GetUserSecurityAttributesRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getUserSecurityAttributesRequest");
    private final static QName _GetUserGroupsRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getUserGroupsRequest");
    private final static QName _GetGroupMembersResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getGroupMembersResponse");
    private final static QName _GetUserSecurityAttributesAndGroupsRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getUserSecurityAttributesAndGroupsRequest");
    private final static QName _GetGroupDNRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getGroupDNRequest");
    private final static QName _GetSecurityAttributesAndGroupsResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getSecurityAttributesAndGroupsResponse");
    private final static QName _GetGroupMembersByDNRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getGroupMembersByDNRequest");
    private final static QName _GetUserInfoAttributesResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getUserInfoAttributesResponse");
    private final static QName _GetUserInfoAttributesRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getUserInfoAttributesRequest");
    private final static QName _GetCitizenshipResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getCitizenshipResponse");
    private final static QName _GetUserCitizenshipRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getUserCitizenshipRequest");
    private final static QName _GetUserOrgRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getUserOrgRequest");
    private final static QName _GetFormalAccessResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getFormalAccessResponse");
    private final static QName _GetAllUserGroupsResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getAllUserGroupsResponse");
    private final static QName _GetUserGroupsResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getUserGroupsResponse");
    private final static QName _GetSecurityAttributesResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getSecurityAttributesResponse");
    private final static QName _GetOrganizationResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getOrganizationResponse");
    private final static QName _GetAffiliationResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getAffiliationResponse");
    private final static QName _GetClearanceResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getClearanceResponse");
    private final static QName _IsMemberOfResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "isMemberOfResponse");
    private final static QName _GetUserAffiliationRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getUserAffiliationRequest");
    private final static QName _IsMemberOfByDNRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "isMemberOfByDNRequest");
    private final static QName _GetGroupDNResponse_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getGroupDNResponse");
    private final static QName _GetUserFormalAccessRequest_QNAME = new QName("http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", "getUserFormalAccessRequest");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: gov.ic.dodiis.service.security.dias.v2_1.authservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link UserIdentifierType }
     * 
     */
    public UserIdentifierType createUserIdentifierType() {
        return new UserIdentifierType();
    }

    /**
     * Create an instance of {@link GetAllUserGroupsResponseType }
     * 
     */
    public GetAllUserGroupsResponseType createGetAllUserGroupsResponseType() {
        return new GetAllUserGroupsResponseType();
    }

    /**
     * Create an instance of {@link GetUserClearanceResponseType }
     * 
     */
    public GetUserClearanceResponseType createGetUserClearanceResponseType() {
        return new GetUserClearanceResponseType();
    }

    /**
     * Create an instance of {@link IsMemberOfResponseType }
     * 
     */
    public IsMemberOfResponseType createIsMemberOfResponseType() {
        return new IsMemberOfResponseType();
    }

    /**
     * Create an instance of {@link GetGroupDNRequestType }
     * 
     */
    public GetGroupDNRequestType createGetGroupDNRequestType() {
        return new GetGroupDNRequestType();
    }

    /**
     * Create an instance of {@link GetAffiliationResponseType }
     * 
     */
    public GetAffiliationResponseType createGetAffiliationResponseType() {
        return new GetAffiliationResponseType();
    }

    /**
     * Create an instance of {@link GetGroupMembersRequestType }
     * 
     */
    public GetGroupMembersRequestType createGetGroupMembersRequestType() {
        return new GetGroupMembersRequestType();
    }

    /**
     * Create an instance of {@link UserClearanceInfoType }
     * 
     */
    public UserClearanceInfoType createUserClearanceInfoType() {
        return new UserClearanceInfoType();
    }

    /**
     * Create an instance of {@link UserGroupRequestType }
     * 
     */
    public UserGroupRequestType createUserGroupRequestType() {
        return new UserGroupRequestType();
    }

    /**
     * Create an instance of {@link ProjectType }
     * 
     */
    public ProjectType createProjectType() {
        return new ProjectType();
    }

    /**
     * Create an instance of {@link GetCitizenshipResponseType }
     * 
     */
    public GetCitizenshipResponseType createGetCitizenshipResponseType() {
        return new GetCitizenshipResponseType();
    }

    /**
     * Create an instance of {@link GetUserGroupsResponseType }
     * 
     */
    public GetUserGroupsResponseType createGetUserGroupsResponseType() {
        return new GetUserGroupsResponseType();
    }

    /**
     * Create an instance of {@link GetFormalAccessResponseType }
     * 
     */
    public GetFormalAccessResponseType createGetFormalAccessResponseType() {
        return new GetFormalAccessResponseType();
    }

    /**
     * Create an instance of {@link GetSecurityAttributesAndGroupsResponseType }
     * 
     */
    public GetSecurityAttributesAndGroupsResponseType createGetSecurityAttributesAndGroupsResponseType() {
        return new GetSecurityAttributesAndGroupsResponseType();
    }

    /**
     * Create an instance of {@link GetSecurityAttributesResponseType }
     * 
     */
    public GetSecurityAttributesResponseType createGetSecurityAttributesResponseType() {
        return new GetSecurityAttributesResponseType();
    }

    /**
     * Create an instance of {@link ErrorType }
     * 
     */
    public ErrorType createErrorType() {
        return new ErrorType();
    }

    /**
     * Create an instance of {@link WhitePageAttributesType }
     * 
     */
    public WhitePageAttributesType createWhitePageAttributesType() {
        return new WhitePageAttributesType();
    }

    /**
     * Create an instance of {@link IsMemberOfRequestType }
     * 
     */
    public IsMemberOfRequestType createIsMemberOfRequestType() {
        return new IsMemberOfRequestType();
    }

    /**
     * Create an instance of {@link DistinguisedNameListType }
     * 
     */
    public DistinguisedNameListType createDistinguisedNameListType() {
        return new DistinguisedNameListType();
    }

    /**
     * Create an instance of {@link GetOrganizationResponseType }
     * 
     */
    public GetOrganizationResponseType createGetOrganizationResponseType() {
        return new GetOrganizationResponseType();
    }

    /**
     * Create an instance of {@link GetGroupMembersByDNRequestType }
     * 
     */
    public GetGroupMembersByDNRequestType createGetGroupMembersByDNRequestType() {
        return new GetGroupMembersByDNRequestType();
    }

    /**
     * Create an instance of {@link GetGroupDNResponseType }
     * 
     */
    public GetGroupDNResponseType createGetGroupDNResponseType() {
        return new GetGroupDNResponseType();
    }

    /**
     * Create an instance of {@link IsMemberOfByDNRequestType }
     * 
     */
    public IsMemberOfByDNRequestType createIsMemberOfByDNRequestType() {
        return new IsMemberOfByDNRequestType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAllUserGroupsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getAllGroupsResponse")
    public JAXBElement<GetAllUserGroupsResponseType> createGetAllGroupsResponse(GetAllUserGroupsResponseType value) {
        return new JAXBElement<GetAllUserGroupsResponseType>(_GetAllGroupsResponse_QNAME, GetAllUserGroupsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetGroupMembersRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getGroupMembersRequest")
    public JAXBElement<GetGroupMembersRequestType> createGetGroupMembersRequest(GetGroupMembersRequestType value) {
        return new JAXBElement<GetGroupMembersRequestType>(_GetGroupMembersRequest_QNAME, GetGroupMembersRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserIdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getUserClearanceRequest")
    public JAXBElement<UserIdentifierType> createGetUserClearanceRequest(UserIdentifierType value) {
        return new JAXBElement<UserIdentifierType>(_GetUserClearanceRequest_QNAME, UserIdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserIdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getAllUserGroupsRequest")
    public JAXBElement<UserIdentifierType> createGetAllUserGroupsRequest(UserIdentifierType value) {
        return new JAXBElement<UserIdentifierType>(_GetAllUserGroupsRequest_QNAME, UserIdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IsMemberOfRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "isMemberOfRequest")
    public JAXBElement<IsMemberOfRequestType> createIsMemberOfRequest(IsMemberOfRequestType value) {
        return new JAXBElement<IsMemberOfRequestType>(_IsMemberOfRequest_QNAME, IsMemberOfRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserIdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getUserSecurityAttributesRequest")
    public JAXBElement<UserIdentifierType> createGetUserSecurityAttributesRequest(UserIdentifierType value) {
        return new JAXBElement<UserIdentifierType>(_GetUserSecurityAttributesRequest_QNAME, UserIdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserGroupRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getUserGroupsRequest")
    public JAXBElement<UserGroupRequestType> createGetUserGroupsRequest(UserGroupRequestType value) {
        return new JAXBElement<UserGroupRequestType>(_GetUserGroupsRequest_QNAME, UserGroupRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DistinguisedNameListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getGroupMembersResponse")
    public JAXBElement<DistinguisedNameListType> createGetGroupMembersResponse(DistinguisedNameListType value) {
        return new JAXBElement<DistinguisedNameListType>(_GetGroupMembersResponse_QNAME, DistinguisedNameListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserGroupRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getUserSecurityAttributesAndGroupsRequest")
    public JAXBElement<UserGroupRequestType> createGetUserSecurityAttributesAndGroupsRequest(UserGroupRequestType value) {
        return new JAXBElement<UserGroupRequestType>(_GetUserSecurityAttributesAndGroupsRequest_QNAME, UserGroupRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetGroupDNRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getGroupDNRequest")
    public JAXBElement<GetGroupDNRequestType> createGetGroupDNRequest(GetGroupDNRequestType value) {
        return new JAXBElement<GetGroupDNRequestType>(_GetGroupDNRequest_QNAME, GetGroupDNRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSecurityAttributesAndGroupsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getSecurityAttributesAndGroupsResponse")
    public JAXBElement<GetSecurityAttributesAndGroupsResponseType> createGetSecurityAttributesAndGroupsResponse(GetSecurityAttributesAndGroupsResponseType value) {
        return new JAXBElement<GetSecurityAttributesAndGroupsResponseType>(_GetSecurityAttributesAndGroupsResponse_QNAME, GetSecurityAttributesAndGroupsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetGroupMembersByDNRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getGroupMembersByDNRequest")
    public JAXBElement<GetGroupMembersByDNRequestType> createGetGroupMembersByDNRequest(GetGroupMembersByDNRequestType value) {
        return new JAXBElement<GetGroupMembersByDNRequestType>(_GetGroupMembersByDNRequest_QNAME, GetGroupMembersByDNRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WhitePageAttributesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getUserInfoAttributesResponse")
    public JAXBElement<WhitePageAttributesType> createGetUserInfoAttributesResponse(WhitePageAttributesType value) {
        return new JAXBElement<WhitePageAttributesType>(_GetUserInfoAttributesResponse_QNAME, WhitePageAttributesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserIdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getUserInfoAttributesRequest")
    public JAXBElement<UserIdentifierType> createGetUserInfoAttributesRequest(UserIdentifierType value) {
        return new JAXBElement<UserIdentifierType>(_GetUserInfoAttributesRequest_QNAME, UserIdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCitizenshipResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getCitizenshipResponse")
    public JAXBElement<GetCitizenshipResponseType> createGetCitizenshipResponse(GetCitizenshipResponseType value) {
        return new JAXBElement<GetCitizenshipResponseType>(_GetCitizenshipResponse_QNAME, GetCitizenshipResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserIdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getUserCitizenshipRequest")
    public JAXBElement<UserIdentifierType> createGetUserCitizenshipRequest(UserIdentifierType value) {
        return new JAXBElement<UserIdentifierType>(_GetUserCitizenshipRequest_QNAME, UserIdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserIdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getUserOrgRequest")
    public JAXBElement<UserIdentifierType> createGetUserOrgRequest(UserIdentifierType value) {
        return new JAXBElement<UserIdentifierType>(_GetUserOrgRequest_QNAME, UserIdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetFormalAccessResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getFormalAccessResponse")
    public JAXBElement<GetFormalAccessResponseType> createGetFormalAccessResponse(GetFormalAccessResponseType value) {
        return new JAXBElement<GetFormalAccessResponseType>(_GetFormalAccessResponse_QNAME, GetFormalAccessResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAllUserGroupsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getAllUserGroupsResponse")
    public JAXBElement<GetAllUserGroupsResponseType> createGetAllUserGroupsResponse(GetAllUserGroupsResponseType value) {
        return new JAXBElement<GetAllUserGroupsResponseType>(_GetAllUserGroupsResponse_QNAME, GetAllUserGroupsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserGroupsResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getUserGroupsResponse")
    public JAXBElement<GetUserGroupsResponseType> createGetUserGroupsResponse(GetUserGroupsResponseType value) {
        return new JAXBElement<GetUserGroupsResponseType>(_GetUserGroupsResponse_QNAME, GetUserGroupsResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSecurityAttributesResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getSecurityAttributesResponse")
    public JAXBElement<GetSecurityAttributesResponseType> createGetSecurityAttributesResponse(GetSecurityAttributesResponseType value) {
        return new JAXBElement<GetSecurityAttributesResponseType>(_GetSecurityAttributesResponse_QNAME, GetSecurityAttributesResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetOrganizationResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getOrganizationResponse")
    public JAXBElement<GetOrganizationResponseType> createGetOrganizationResponse(GetOrganizationResponseType value) {
        return new JAXBElement<GetOrganizationResponseType>(_GetOrganizationResponse_QNAME, GetOrganizationResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAffiliationResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getAffiliationResponse")
    public JAXBElement<GetAffiliationResponseType> createGetAffiliationResponse(GetAffiliationResponseType value) {
        return new JAXBElement<GetAffiliationResponseType>(_GetAffiliationResponse_QNAME, GetAffiliationResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserClearanceResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getClearanceResponse")
    public JAXBElement<GetUserClearanceResponseType> createGetClearanceResponse(GetUserClearanceResponseType value) {
        return new JAXBElement<GetUserClearanceResponseType>(_GetClearanceResponse_QNAME, GetUserClearanceResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IsMemberOfResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "isMemberOfResponse")
    public JAXBElement<IsMemberOfResponseType> createIsMemberOfResponse(IsMemberOfResponseType value) {
        return new JAXBElement<IsMemberOfResponseType>(_IsMemberOfResponse_QNAME, IsMemberOfResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserIdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getUserAffiliationRequest")
    public JAXBElement<UserIdentifierType> createGetUserAffiliationRequest(UserIdentifierType value) {
        return new JAXBElement<UserIdentifierType>(_GetUserAffiliationRequest_QNAME, UserIdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IsMemberOfByDNRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "isMemberOfByDNRequest")
    public JAXBElement<IsMemberOfByDNRequestType> createIsMemberOfByDNRequest(IsMemberOfByDNRequestType value) {
        return new JAXBElement<IsMemberOfByDNRequestType>(_IsMemberOfByDNRequest_QNAME, IsMemberOfByDNRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetGroupDNResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getGroupDNResponse")
    public JAXBElement<GetGroupDNResponseType> createGetGroupDNResponse(GetGroupDNResponseType value) {
        return new JAXBElement<GetGroupDNResponseType>(_GetGroupDNResponse_QNAME, GetGroupDNResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserIdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dias.security.service.dodiis.ic.gov/v2.1/authservice/", name = "getUserFormalAccessRequest")
    public JAXBElement<UserIdentifierType> createGetUserFormalAccessRequest(UserIdentifierType value) {
        return new JAXBElement<UserIdentifierType>(_GetUserFormalAccessRequest_QNAME, UserIdentifierType.class, null, value);
    }

}
