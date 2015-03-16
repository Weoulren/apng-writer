### About APNG Writer ###
APNG Writer is an app and library for writing animated PNG from a list of images written in java.


It started with a need for me to create APNGs from a sequence of image in java in a headless envionment and that I haven't not found a suitable thing for that. As I wrote the prototype satisfing my needs, it grew more and more, up to the scale when I realized may be helpfull for someome else. 

Features:

- writes a fully compliaint apng from a list of images (with known count) or a sequence of images (not known count, with some limitations)
- supports all PNG byte filters
- supports custom optimizations with result fully compliant to the standart
- pure java, runs in a headless environment

Lacks:

- documentation
- tests
- decent cli
- creating apng from a 32 bit source

Suggestions and contributions are welcome! 

### Optimizations and special filters ###
When I received the first results I was badly suprised with the size of the files coming out. A 50Mb file for a forty 770x900 images? Seriously? No way! 

PNG allows to specify filter type on a per image line basis, so aside from the standart PNG filters [(none, nub, up, avarage & paeth)](http://www.w3.org/TR/PNG/#9Filters), I've implemented a couple of filters trying to search for an optimal combination of standart filters for an image. This isn't a fullscale search, it just looks up for a local minimum for each line. Practically it gives 5% to 25% of nonfiltered image.

More over, APNG fcTL chunk allows to specify that a frame contains only a part of the image, which suggests to store only the part of an image that have changed. It's optimization: 

- Identity - leave image as is
- Slicer - calculate the bounding box of the pixels that differ from previous image and store only what's inside it notifying the decoder in fcTL chunk
- ARGB Subtractor - convert the image to the 32 bit, then subtract it from the previous, equal pixels set to transparent
- ARGB Slicing Subtractor - a combination of the previous two
