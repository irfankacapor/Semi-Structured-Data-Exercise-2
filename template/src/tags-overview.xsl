<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="html"/>
    <!-- Insert the XSLT Stylesheet here -->
    <xsl:template match="shipment">
        <html>
            <head>
                <title> Shipment - Tags Overview</title>
                <style>
                    body {
                    font-family: Verdana, sans-serif;
                    }
                    table {
                    font-family: Verdana, sans-serif;
                    border-collapse: collapse;
                    width: 31em;
                    }

                    td, th {
                    border: 1px solid #080808;
                    padding: 8px;
                    vertical-align: top;
                    }

                    tr:hover {background-color: #fdf3fd;}

                    th {
                    padding-top: 2px;
                    padding-bottom: 2px;
                    text-align: center;
                    background-color: #6c3b6c;
                    color: white;
                    }
                </style>
            </head>
            <body>
                <h1>Tags Overview</h1>
                <hr/>
                <xsl:apply-templates select="//t[@tagname]"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="t">
        <h2><xsl:value-of select="@tagname"/></h2>
        <b>Description:</b><br/>
        <xsl:choose>
            <xsl:when test="normalize-space(.) != ''">
                <xsl:value-of select="normalize-space(.)"/>
            </xsl:when>
            <xsl:otherwise>
                No further information on <xsl:value-of select="@tagname"/>.
            </xsl:otherwise>
        </xsl:choose>
        <br></br>
        <br></br>
        Below is a list of all Ships or Products tagged with <xsl:value-of select="@tagname"/>, either directly, or indirectly via association from their transportation.
        <table>
            <!-- With the ifs bellow I checked whether there are any ships or products associated with the name and only then created the table-->
            <xsl:if test="//ship/tags/tag=@tagname">
            <td><xsl:call-template name="info"><xsl:with-param name="mode" select="'ship'"/><xsl:with-param name="tagname" select="@tagname"/></xsl:call-template></td>
            </xsl:if>
            <xsl:if test="//product/tags/tag=@tagname">
                <br/>
                <td><xsl:call-template name="info"><xsl:with-param name="mode" select="'product'"/><xsl:with-param name="tagname" select="@tagname"/></xsl:call-template></td>
            </xsl:if>
        </table>
        <hr></hr>
    </xsl:template>

    <xsl:template name="info">
        <xsl:param name="mode"/>
        <xsl:param name="tagname"/>
        <xsl:choose>
            <xsl:when test="$mode = 'ship'">
                <xsl:variable name="ships" select="//ship[tags/tag=$tagname or ref/@sid=//product[tags/tag=$tagname]/ref/@sid]"/>
                <xsl:if test="$ships">
                    <table>
                        <tr>
                            <th><h3>Ships</h3></th>
                        </tr>
                        <xsl:for-each select="$ships">
                            <xsl:sort select="name"/>
                            <tr>
                                <td>
                                    <xsl:call-template name="ship"/>
                                </td>
                            </tr>
                        </xsl:for-each>
                        <tr>
                            <td><b>Found: </b> <xsl:value-of select="count($ships)"/> Ship(s)</td>
                        </tr>
                    </table>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="products" select="//product[tags/tag=$tagname or ref/@sid=//ship[tags/tag=$tagname]/@sid]"/>
                <xsl:if test="$products">
                    <table>
                        <tr>
                            <th><h3>Products</h3></th>
                        </tr>
                        <xsl:for-each select="$products">
                            <xsl:sort select="label/destination[1]"/>
                            <tr>
                                <td>
                                    <xsl:call-template name="product"/>
                                </td>
                            </tr>
                        </xsl:for-each>
                        <tr>
                            <td><b>Found: </b><xsl:value-of select="count($products)"/> Product(s)</td>
                        </tr>
                    </table>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="ship" name="ship">
        <xsl:value-of select="name"/>
        <xsl:text> ( </xsl:text>
        <xsl:value-of select="info/@firstTour"/>
        <xsl:if test="info/@lastTour">
            <xsl:text> to </xsl:text>
            <xsl:value-of select="info/@lastTour"/>
        </xsl:if>
        <xsl:text>, constructed in </xsl:text>
        <xsl:value-of select="info/@placeOfConstruction"/>
        <xsl:text> )</xsl:text>
    </xsl:template>


    <xsl:template match="product" name="product">
        <b>Title: </b><xsl:value-of select="name"/>,  <b>Type: </b> <xsl:value-of select="upper-case(name(type/*))"/>
        <br></br>
        <br></br>
        <b>Label:</b>
        <i><xsl:value-of select="upper-case(@type)"/>
        <br/>
        <xsl:apply-templates select="label"/>
        </i>
        <br/>
        <br></br>
        <xsl:variable name="matchingShip" select="//ship[@sid=current()/label/ref/@sid]"/>
        <xsl:choose>
            <xsl:when test="$matchingShip">

                <b>Transported By:</b>
                <ul>
                    <xsl:for-each select="$matchingShip">
                        <li><xsl:value-of select="name"/>
                            ( <xsl:value-of select="info/@firstTour"/> to <xsl:value-of select="info/@lastTour"/>,
                            constructed in <xsl:value-of select="info/@placeOfConstruction"/> )</li>
                    </xsl:for-each>
                </ul>
            </xsl:when>
            <xsl:otherwise>
                No shipment detail.
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
