use std::sync::{Arc, Mutex, MutexGuard};

use android_logger::Config;
use convnet_rust::{Net, Prediction, Sample, Specification, TrainStats, Trainer, Vol};

uniffi::setup_scaffolding!();

const MNIST_BYTES: &[u8] = include_bytes!("../../../models/mnist-net.bin");

use backtrace::Backtrace;
use log::{error, LevelFilter};

#[derive(uniffi::Record)]
pub struct WeightImage {
    image: Vec<u8>,
    width: u32,
    height: u32,
}

#[derive(uniffi::Record)]
pub struct Layer {
    name: String,
    in_sx: u32,
    in_sy: u32,
    in_depth: u32,
    // images: Vec<WeightImage>,
}

#[allow(dead_code)]
struct Inner {
    // spec: Specification,
    net: Net,

    trainer: Trainer,
}

#[derive(uniffi::Object)]
pub struct NeuralNetwork {
    inner: Arc<Mutex<Inner>>,
}

impl NeuralNetwork {
    fn lock(&self) -> MutexGuard<Inner> {
        self.inner.lock().expect("failed to lock resouces")
    }
}

#[uniffi::export]
impl NeuralNetwork {
    #[uniffi::constructor]
    pub fn new(spec: Specification) -> Self {
        // let spec: Specification = serde_json::from_str(value).unwrap();

        Self {
            inner: Arc::new(Mutex::new(Inner {
                net: Net::new(&spec.layers, spec.final_layer),
                // spec,
                trainer: Trainer::builder().epoch(5).build(),
            })),
        }
    }

    // pub fn specification(&self) -> String {
    //     let this = self.lock();
    //     serde_json::to_string_pretty::<Specification>(&this.spec)
    //         .expect("serialization should not fail")
    // }

    #[uniffi::constructor]
    pub fn load_mnist() -> NeuralNetwork {
        Self {
            inner: Arc::new(Mutex::new(Inner {
                net: Net::load_from_bytes(MNIST_BYTES).expect("should be a valid serilialized net"),
                // spec: Specification::default(),
                trainer: Trainer::builder().epoch(5).build(),
            })),
        }
    }

    pub fn predict(&self, bytes: &[u8], width: u32, height: u32) -> u32 {
        let mut this = self.lock();
        this.net
            .forward(&Vol::from_grayscale_image(bytes, width, height), false);
        this.net.get_prediction() as u32
    }

    pub fn predicts(&self, bytes: &[u8], width: u32, height: u32) -> Vec<Prediction> {
        let mut this = self.lock();
        this.net
            .forward(&Vol::from_grayscale_image(bytes, width, height), false);
        this.net.get_predictions()
    }

    pub fn layers(&self) -> Vec<Layer> {
        let this = self.lock();
        let (hidden_layers, final_layer) = this.net.layers();

        let names = hidden_layers
            .iter()
            .map(|x| (x.name().to_owned(), x.out_sx(), x.out_sy(), x.out_depth()))
            .chain(std::iter::once((
                final_layer.name().to_owned(),
                final_layer.out_sx(),
                final_layer.out_sy(),
                final_layer.out_depth(),
            )))
            .collect::<Vec<_>>();

        let mut result = Vec::with_capacity(hidden_layers.len() + 1);
        for (name, out_sx, out_sy, out_depth) in names.into_iter() {
            result.push(Layer {
                name,
                in_sx: out_sx as u32,
                in_sy: out_sy as u32,
                in_depth: out_depth as u32,
            });
        }
        result
    }

    pub fn layer_images(&self, index: u32) -> Option<Vec<WeightImage>> {
        let this = self.lock();
        let (hidden_layers, final_layer) = this.net.layers();

        let weights = hidden_layers
            .iter()
            .map(|x| x.weights())
            .chain(std::iter::once(final_layer.weights()))
            .collect::<Vec<_>>();

        let layer = weights.get(index as usize)?;

        let mut images = Vec::with_capacity(weights.len());
        for weight in layer {
            let width = weight.sx();
            let height = weight.sy();
            let depth = weight.depth();
            let mut image = Vec::with_capacity(width * height);
            for z in 0..depth {
                for y in 0..height {
                    for x in 0..width {
                        let x = weight.get(x, y, z);
                        image.push(((x + 0.5) * 255.0) as u8);
                    }
                }
            }
            images.push(WeightImage {
                image,
                width: width as u32,
                height: height as u32,
            })
        }

        Some(images)
    }

    pub fn weight_images(&self, index: u32) -> Option<Vec<WeightImage>> {
        let this = self.lock();

        let vol = this.net.weights().get(index as usize)?;

        let width = vol.sx();
        let height = vol.sy();
        let depth = vol.depth();
        let mut images = Vec::with_capacity(depth);
        for z in 0..depth {
            let mut image = Vec::with_capacity(width * height);
            for y in 0..height {
                for x in 0..width {
                    let x = vol.get(x, y, z);
                    image.push(((x + 0.5) * 255.0) as u8);
                }
            }
            images.push(WeightImage {
                image,
                width: width as u32,
                height: height as u32,
            })
        }

        Some(images)
    }

    pub fn to_bytes(&self) -> Vec<u8> {
        let this = self.lock();
        this.net.save_as_bytes().unwrap()
    }

    #[uniffi::constructor]
    pub fn from_bytes(bytes: &[u8]) -> Self {
        Self {
            inner: Arc::new(Mutex::new(Inner {
                net: Net::load_from_bytes(bytes).unwrap(), // spec,
                trainer: Trainer::builder().epoch(5).build(),
            })),
        }
    }

    pub fn set_samples(&self, samples: &[OtherSample], epoch: u32) {
        let mut this = self.lock();
        this.trainer = Trainer::builder()
            .epoch(epoch)
            .batch_size(1)
            .method(convnet_rust::Method::Adadlta {
                eps: 1e-6,
                ro: 0.95,
            })
            .l2_decay(0.001)
            .epoch(5)
            .samples(
                samples
                    .iter()
                    .map(|x| Sample::from_grayscale_image(&x.bytes, x.width, x.height, x.label))
                    .collect(),
            )
            .build_without_validation();
    }

    pub fn train(&self) -> NetTrain {
        // android_logger::init_once(
        //     Config::default()
        //         .with_max_level(LevelFilter::Trace) // limit log level
        //         .with_tag("mytag"), // logs will show under mytag tag
        //                             // .with_filter(
        //                             //     // configure messages for specific crate
        //                             //     FilterBuilder::new()
        //                             //         .parse("debug,hello::crate=error")
        //                             //         .build(),
        //                             // ),
        // );

        // std::panic::set_hook(Box::new(|i| {
        //     let trace = Backtrace::new();
        //     error!("{trace:?}");
        //     error!("{:?}", i.payload().downcast_ref::<&str>())
        // }));

        let mut this = self.lock();
        // std::env::set_var("RUST_BACKTRACE", "1");

        if this.trainer.samples.is_empty() {
            return NetTrain {
                finished: true,
                ..Default::default()
            };
        }

        let mut stats = Default::default();

        let mut trainer = Trainer::builder().build();
        std::mem::swap(&mut this.trainer, &mut trainer);

        assert!(!trainer.samples.is_empty(), "should have samples");
        let finished = trainer.train_without_validation(&mut this.net, &mut stats);

        std::mem::swap(&mut this.trainer, &mut trainer);
        NetTrain { finished, stats }
    }
}

#[derive(Debug, Clone, Copy, Default, uniffi::Record)]
pub struct NetTrain {
    pub finished: bool,
    pub stats: TrainStats,
}

#[derive(Debug, Clone, Default, uniffi::Record)]
pub struct OtherSample {
    pub bytes: Vec<u8>,
    pub width: u32,
    pub height: u32,
    pub label: u32,
}

#[uniffi::export]
pub fn specification_to_json(spec: &Specification) -> String {
    serde_json::to_string(spec).unwrap()
}

#[uniffi::export]
pub fn specification_from_json(s: &str) -> Specification {
    serde_json::from_str(s).unwrap()
}
