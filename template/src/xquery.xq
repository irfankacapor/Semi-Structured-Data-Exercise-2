declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";
declare option output:method "xml";
declare option output:indent "yes";

<largeOrdersOnShips>
{
    for $ship in //ship
    let $sid := $ship/@sid
    let $name := $ship/name
    let $firstTour := xs:date($ship/info/@firstTour)
    for $destination in distinct-values(//product/label/destination)
    let $products := //product[label/ref[@sid=$sid] and label/destination=$destination]
    where count($products) > 3
    order by $firstTour
    return
        <ship name="{$name}" destination="{$destination}">
            <productCount>{count($products)}</productCount>
            {
                for $product at $pos in $products[position() <= 3]
                return <product>{$product/name}</product>
            }
        </ship>
}
</largeOrdersOnShips>
