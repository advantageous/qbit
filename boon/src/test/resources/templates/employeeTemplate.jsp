<c:forEach items='${fn:sortBy(employees, firstName, lastName)}' >
    <c:include resource='/templates/empTemp.jsp' emp='item'></c:include>
</c:forEach>