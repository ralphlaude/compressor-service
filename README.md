## Setup

1. SBT 1.0.4
2. Java 8
3. Scala 2.12.4
4. cd xxx

## Usage

1. In the first console start the docker container:

    1. `docker pull 21re/coding-challenge`

    1. `docker run -p 8080:80 21re/coding-challenge`


2. In the second console run the project:

    1. `sbt run`
 
3. In the browser:

    1. call http://localhost:9000/{index}


## Testing

 `sbt test`
 

## Documentation

The documentation is in here and in the source code:
1. The class CompressorClient implements provides the encoding of the data and the access to the encoded over the index.
The index starts by 0.
2. The class RunsServiceClient access the external runs service and is launched by a scheduler. 
3. The class CompressionService configures the access endpoint, starts the service and the scheduler for the external service.
This scheduler runs every 60 seconds.
