xsd-downloader
==============

Al utilizar un servicio web descripto por un archivo wsdl. Lo mejor es descargar al classpath todos los archivos xsd que sean importados para acelerar la creación del cliente y a su vez no depender de la fiabilidad del servidor que los proporciona.
Los imports normalmente son muchos y bajarlos a mano es un proceso tedioso. Para evitar esto podemos utilizar esta pequeña aplicación java que descarga todos los imports de un WSDL o XMLSchema que encuentre.

Modo de uso:

java -jar xsd-downloader.jar <WSDL-Location> <Prefix>

Luego cambiar la extensión del primer archivo creado (del tipo <prefix>.xsd) a wsdl. Ya que el primer archivo que crea es el wsdl con las nuevas referencias en los imports.


Ejemplo:
En este ejemplo el programa creará en el directorio donde se ejecute, archivos del tipo netsuite-1.xsd, netsuite-2.xsd, etc.

java -jar xsd-downloader.jar /home/pablo/git/netsuite-connector/src/main/resources/netsuite.wsdl netsuite
mv netsuite.xsd netsuite.wsdl


Thanks Everit! - https://www.everit.biz/web/guest/blog/-/blogs/downloading-wsdl-files-for-offline-use
